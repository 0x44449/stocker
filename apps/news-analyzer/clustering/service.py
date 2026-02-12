import numpy as np
from sklearn.cluster import DBSCAN
from sqlalchemy.orm import Session

from models import NewsExtraction, NewsEmbedding, NewsRaw

# extraction 조회 시 고정 조건
LLM_MODEL = "qwen2.5:7b"
PROMPT_VERSION = "v1"


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
        return {"keyword": keyword, "total_count": 0, "clusters": [], "noise": []}

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
        return {"keyword": keyword, "total_count": 0, "clusters": [], "noise": []}

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
    clusters = sorted(
        [
            {"cluster_id": int(cid), "count": int(len(articles)), "articles": articles}
            for cid, articles in cluster_map.items()
        ],
        key=lambda c: c["count"],
        reverse=True,
    )

    total_count = len(valid_news_ids)

    return {
        "keyword": keyword,
        "total_count": total_count,
        "clusters": clusters,
        "noise": noise_articles,
    }
