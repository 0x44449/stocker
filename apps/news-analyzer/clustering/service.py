import logging

import numpy as np
from langchain_ollama import OllamaLLM
from sklearn.cluster import DBSCAN
from sqlalchemy.orm import Session

from config import OLLAMA_BASE_URL
from sqlalchemy import or_

from models import NewsExtraction, NewsEmbedding, NewsRaw, StockAlias, SubsidiaryMapping, StockMaster, StockPriceDailyRaw

logger = logging.getLogger(__name__)

# extraction 조회 시 고정 조건
LLM_MODEL = "exaone3.5:7.8b"
PROMPT_VERSION = "v1"

TOPIC_PROMPT = """아래 뉴스 제목들을 대표하는 뉴스 헤드라인을 하나 만들어줘.
실제 뉴스 기사 제목처럼 작성해. 다른 설명 없이 헤드라인만 출력해.

제목들:
{titles}

헤드라인:"""

SUMMARY_PROMPT = """아래 뉴스 본문들을 읽고 핵심 내용을 2~3줄로 요약해줘.
다른 설명 없이 요약만 출력해.

{bodies}

요약:"""


def _summarize_topic(titles: list[str]) -> str:
    """기사 제목 목록에서 대표 헤드라인 생성"""
    llm = OllamaLLM(model="exaone3.5:7.8b", base_url=OLLAMA_BASE_URL)
    prompt = TOPIC_PROMPT.format(titles="\n".join(f"- {t}" for t in titles))
    result = llm.invoke(prompt).strip()
    logger.info(f"토픽 요약 완료 - 제목 수: {len(titles)}, 결과: {result}")
    return result


def _summarize_body(texts: list[str]) -> str:
    """기사 본문들을 2~3줄로 요약"""
    logger.info(f"본문 요약 시작 - 기사 수: {len(texts)}")
    llm = OllamaLLM(model="exaone3.5:7.8b", base_url=OLLAMA_BASE_URL)
    # 상위 5개, 각 300자 제한
    truncated = [t[:300] for t in texts[:5]]
    bodies = "\n\n".join(f"[기사 {i+1}]\n{t}" for i, t in enumerate(truncated))
    prompt = SUMMARY_PROMPT.format(bodies=bodies)
    result = llm.invoke(prompt).strip()
    logger.info(f"본문 요약 완료 - 기사 수: {len(truncated)}, 결과: {result}")
    return result


def _get_latest_price(db: Session, stock_code: str) -> dict | None:
    """stock_code의 최근 거래일 주가 조회"""
    row = (
        db.query(StockPriceDailyRaw)
        .filter(StockPriceDailyRaw.stock_code == stock_code)
        .order_by(StockPriceDailyRaw.trd_dd.desc())
        .first()
    )
    if not row:
        return None
    return {
        "stock_code": row.stock_code,
        "date": str(row.trd_dd),
        "close": int(row.close) if row.close else None,
        "diff_rate": float(row.diff_rate) if row.diff_rate else None,
    }


def _find_related_stock(db: Session, rows, main_stock_code: str, search_keywords: list[str]) -> dict | None:
    """클러스터 내 뉴스 keywords에서 주체 종목 외 가장 많이 언급된 종목 1건"""
    # alias/subsidiary → stock_code 매핑
    alias_map = {r.alias: r.stock_code for r in db.query(StockAlias).all()}
    sub_map = {r.subsidiary_name: r.stock_code for r in db.query(SubsidiaryMapping).all()}
    # stock name → stock_code 매핑
    name_map = {r.name_kr_short: r.stock_code for r in db.query(StockMaster.name_kr_short, StockMaster.stock_code).all()}

    counts = {}
    search_set = set(search_keywords)
    for row in rows:
        for kw in row.keywords or []:
            if kw in search_set:
                continue
            code = name_map.get(kw) or alias_map.get(kw) or sub_map.get(kw)
            if code and code != main_stock_code:
                counts[code] = counts.get(code, 0) + 1

    if not counts:
        return None

    top_code = max(counts, key=counts.get)
    price = _get_latest_price(db, top_code)
    stock_name_row = db.query(StockMaster.name_kr_short).filter(StockMaster.stock_code == top_code).first()

    return {
        "stock_name": stock_name_row.name_kr_short if stock_name_row else None,
        "stock_code": top_code,
        "mention_count": counts[top_code],
        "close": price["close"] if price else None,
        "diff_rate": price["diff_rate"] if price else None,
    }


def cluster_news(db: Session, keyword: str, days: int, eps: float):
    """keyword 관련 뉴스를 DBSCAN으로 클러스터링"""
    from datetime import date, datetime, timedelta
    logger.info(f"클러스터링 시작 - 키워드: {keyword}, 기간(일): {days}, eps: {eps}")

    # days=2면 오늘+어제 → 어제 0시부터
    start = datetime.combine(date.today() - timedelta(days=days - 1), datetime.min.time())

    # keyword(종목명)에 해당하는 alias, 자회사명 조회
    aliases = (
        db.query(StockAlias.alias)
        .filter(StockAlias.stock_name == keyword)
        .all()
    )
    subsidiaries = (
        db.query(SubsidiaryMapping.subsidiary_name)
        .filter(SubsidiaryMapping.stock_name == keyword)
        .all()
    )
    # 종목명 + alias + 자회사명을 합친 검색 키워드 목록
    search_keywords = [keyword] + [row.alias for row in aliases] + [row.subsidiary_name for row in subsidiaries]

    # keyword → stock_code 조회
    stock = db.query(StockMaster.stock_code).filter(StockMaster.name_kr_short == keyword).first()

    # keyword 관련 뉴스의 embedding, title을 한 번에 조회 (JOIN으로 embedding 없는 뉴스는 자동 제외)
    rows = (
        db.query(NewsExtraction.news_id, NewsEmbedding.embedding, NewsRaw.title, NewsExtraction.keywords)
        .join(NewsEmbedding, NewsEmbedding.news_id == NewsExtraction.news_id)
        .join(NewsRaw, NewsRaw.id == NewsExtraction.news_id)
        .filter(
            or_(*[NewsExtraction.keywords.op("@>")(f'["{kw}"]') for kw in search_keywords]),
            NewsExtraction.published_at >= start,
            NewsExtraction.llm_model == LLM_MODEL,
            NewsExtraction.prompt_version == PROMPT_VERSION,
        )
        .all()
    )
    logger.info(f"뉴스 조회 완료 - {len(rows)}건")

    if not rows:
        return {"keyword": keyword, "total_count": 0, "stock_price": _get_latest_price(db, stock.stock_code) if stock else None, "related_stock": None, "topic": None, "clusters": [], "noise": []}

    valid_news_ids = []
    vectors = []
    title_map = {}
    for row in rows:
        valid_news_ids.append(row.news_id)
        vectors.append(np.array(row.embedding))
        title_map[row.news_id] = row.title

    if not vectors:
        return {"keyword": keyword, "total_count": 0, "stock_price": _get_latest_price(db, stock.stock_code) if stock else None, "related_stock": None, "topic": None, "clusters": [], "noise": []}

    # DBSCAN 클러스터링 (cosine 거리 기반, label=-1은 노이즈)
    matrix = np.stack(vectors)
    clustering = DBSCAN(eps=eps, min_samples=2, metric="cosine").fit(matrix)
    logger.info(f"DBSCAN 완료 - 클러스터 수: {len(set(clustering.labels_)) - (1 if -1 in clustering.labels_ else 0)}")
    labels = clustering.labels_

    # 클러스터별 기사 목록 구성
    cluster_map = {}
    noise_articles = []

    for i, label in enumerate(labels):
        news_id = valid_news_ids[i]
        article = {"news_id": int(news_id), "title": title_map.get(news_id, "")}

        if label == -1:
            noise_articles.append(article)
        else:
            cluster_map.setdefault(int(label), []).append(article)

    sorted_clusters = sorted(
        [
            {"count": int(len(articles)), "articles": articles}
            for articles in cluster_map.values()
        ],
        key=lambda c: c["count"],
        reverse=True,
    )

    total_count = len(valid_news_ids)

    # 가장 큰 클러스터에 LLM 요약 제목 + 본문 요약 생성
    topic = None
    remaining_clusters = sorted_clusters
    if sorted_clusters:
        top = sorted_clusters[0]
        topic_titles = [a["title"] for a in top["articles"]]

        # topic 클러스터 상위 5개 기사의 본문 조회
        topic_news_ids = [a["news_id"] for a in top["articles"][:5]]
        raw_texts = (
            db.query(NewsRaw.id, NewsRaw.raw_text)
            .filter(NewsRaw.id.in_(topic_news_ids))
            .all()
        )
        body_list = [row.raw_text for row in raw_texts if row.raw_text]

        topic = {
            "title": _summarize_topic(topic_titles),
            "summary": _summarize_body(body_list) if body_list else None,
            "count": top["count"],
            "articles": top["articles"],
        }
        remaining_clusters = sorted_clusters[1:]

    return {
        "keyword": keyword,
        "total_count": total_count,
        "stock_price": _get_latest_price(db, stock.stock_code) if stock else None,
        "related_stock": _find_related_stock(db, rows, stock.stock_code, search_keywords) if stock and rows else None,
        "topic": topic,
        "clusters": remaining_clusters,
        "noise": noise_articles,
    }
