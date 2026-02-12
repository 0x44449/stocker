import logging

import numpy as np
from langchain_ollama import OllamaLLM
from sklearn.cluster import DBSCAN
from sqlalchemy.orm import Session

from config import OLLAMA_BASE_URL
from models import NewsExtraction, NewsEmbedding, NewsRaw

logger = logging.getLogger(__name__)

# extraction 조회 시 고정 조건
LLM_MODEL = "qwen2.5:7b"
PROMPT_VERSION = "v1"

TOPIC_PROMPT = """아래 뉴스 제목들을 대표하는 뉴스 헤드라인을 하나 만들어줘.
실제 뉴스 기사 제목처럼 작성해. 다른 설명 없이 헤드라인만 출력해.

제목들:
{titles}

헤드라인:"""


def _summarize_topic(titles: list[str]) -> str:
    """기사 제목 목록에서 공통 주제를 한 줄로 요약"""
    llm = OllamaLLM(model="exaone3.5:7.8b", base_url=OLLAMA_BASE_URL)
    prompt = TOPIC_PROMPT.format(titles="\n".join(f"- {t}" for t in titles))
    result = llm.invoke(prompt).strip()
    logger.info(f"토픽 요약 완료 - 제목 수: {len(titles)}, 결과: {result}")
    return result


def cluster_news(db: Session, keyword: str, days: int, eps: float):
    """keyword 관련 뉴스를 DBSCAN으로 클러스터링"""
    from datetime import date, datetime, timedelta

    # days=2면 오늘+어제 → 어제 0시부터
    start = datetime.combine(date.today() - timedelta(days=days - 1), datetime.min.time())

    # 1. news_extraction에서 keyword + 날짜 범위로 news_id 조회
    extractions = (
        db.query(NewsExtraction.news_id)
        .filter(
            NewsExtraction.keywords.op("@>")(f'["{keyword}"]'),
            NewsExtraction.published_at >= start,
            NewsExtraction.llm_model == LLM_MODEL,
            NewsExtraction.prompt_version == PROMPT_VERSION,
        )
        .all()
    )
    news_ids = [row.news_id for row in extractions]

    if not news_ids:
        return {"keyword": keyword, "total_count": 0, "topic": None, "clusters": [], "noise": []}

    # 2. news_embedding에서 embedding 로드
    embeddings = (
        db.query(NewsEmbedding.news_id, NewsEmbedding.embedding)
        .filter(NewsEmbedding.news_id.in_(news_ids))
        .all()
    )

    # 3. news_raw에서 title 로드
    titles = (
        db.query(NewsRaw.id, NewsRaw.title)
        .filter(NewsRaw.id.in_(news_ids))
        .all()
    )
    title_map = {row.id: row.title for row in titles}

    # embedding이 없는 뉴스는 제외
    valid_news_ids = []
    vectors = []
    for row in embeddings:
        valid_news_ids.append(row.news_id)
        vectors.append(np.array(row.embedding))

    if not vectors:
        return {"keyword": keyword, "total_count": 0, "topic": None, "clusters": [], "noise": []}

    # 4. DBSCAN 클러스터링
    matrix = np.stack(vectors)
    clustering = DBSCAN(eps=eps, min_samples=2, metric="cosine").fit(matrix)
    labels = clustering.labels_

    # 5. 클러스터별 기사 목록 구성
    cluster_map = {}
    noise_articles = []

    for i, label in enumerate(labels):
        news_id = valid_news_ids[i]
        article = {"news_id": int(news_id), "title": title_map.get(news_id, "")}

        if label == -1:
            noise_articles.append(article)
        else:
            cluster_map.setdefault(int(label), []).append(article)

    # count 내림차순 정렬
    sorted_clusters = sorted(
        [
            {"count": int(len(articles)), "articles": articles}
            for articles in cluster_map.values()
        ],
        key=lambda c: c["count"],
        reverse=True,
    )

    total_count = len(valid_news_ids)

    # 6. 가장 큰 클러스터에 LLM 요약 제목 생성
    topic = None
    remaining_clusters = sorted_clusters
    if sorted_clusters:
        top = sorted_clusters[0]
        topic_titles = [a["title"] for a in top["articles"]]
        topic = {
            "title": _summarize_topic(topic_titles),
            "count": top["count"],
            "articles": top["articles"],
        }
        remaining_clusters = sorted_clusters[1:]

    return {
        "keyword": keyword,
        "total_count": total_count,
        "topic": topic,
        "clusters": remaining_clusters,
        "noise": noise_articles,
    }
