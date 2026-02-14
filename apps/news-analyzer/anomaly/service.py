import logging
from collections import defaultdict
from datetime import date, datetime, timedelta

from sqlalchemy.orm import Session

from models import NewsExtraction, StockMaster, StockAlias, SubsidiaryMapping

logger = logging.getLogger(__name__)

# extraction 조회 시 고정 조건
LLM_MODEL = "exaone3.5:7.8b"
PROMPT_VERSION = "v1"

# 이상 탐지 기준값
MIN_TODAY_COUNT = 10
COMPARE_DAYS = 5
ANOMALY_RATIO = 2.0


def detect_anomalies(db: Session):
    """종목별 뉴스 멘션 이상 감지. 최근 24시간 vs 과거 5일 평균 비교."""
    now = datetime.now()
    today_start = now - timedelta(hours=24)
    past_start = datetime.combine(date.today() - timedelta(days=6), datetime.min.time())
    past_end = datetime.combine(date.today() - timedelta(days=1), datetime.min.time())
    logger.info(f"이상 탐지 시작 - 최근24h: {today_start} ~ {now}, 과거: {past_start} ~ {past_end}")

    # 과거~현재 전체 extraction 조회
    rows = (
        db.query(NewsExtraction.news_id, NewsExtraction.keywords, NewsExtraction.published_at)
        .filter(
            NewsExtraction.published_at >= past_start,
            NewsExtraction.published_at <= now,
            NewsExtraction.llm_model == LLM_MODEL,
            NewsExtraction.prompt_version == PROMPT_VERSION,
        )
        .all()
    )
    logger.info(f"이상 탐지 조회 완료 - {len(rows)}건")

    # 정규화 맵 빌드
    name_map = {r.name_kr_short: r.stock_code for r in db.query(StockMaster.name_kr_short, StockMaster.stock_code).all()}
    alias_map = {r.alias: r.stock_code for r in db.query(StockAlias).all()}
    sub_map = {r.subsidiary_name: r.stock_code for r in db.query(SubsidiaryMapping).all()}
    code_to_name = {r.stock_code: r.name_kr_short for r in db.query(StockMaster.stock_code, StockMaster.name_kr_short).all()}
    logger.info(f"정규화 맵 빌드 완료 - name: {len(name_map)}, alias: {len(alias_map)}, subsidiary: {len(sub_map)}")

    # 시간대별 집계 (news_id 기반 중복 제거)
    today_mentions = defaultdict(set)
    past_mentions = defaultdict(set)

    for row in rows:
        for kw in row.keywords or []:
            code = name_map.get(kw) or alias_map.get(kw) or sub_map.get(kw)
            if not code:
                continue
            if row.published_at >= today_start:
                today_mentions[code].add(row.news_id)
            elif row.published_at < past_end:
                past_mentions[code].add(row.news_id)

    logger.info(f"멘션 집계 완료 - 최근24h: {len(today_mentions)}종목, 과거: {len(past_mentions)}종목")

    # 이상치 필터링
    items = []
    for code, today_ids in today_mentions.items():
        today_count = len(today_ids)
        if today_count < MIN_TODAY_COUNT:
            continue

        past_count = len(past_mentions.get(code, set()))
        avg_count = past_count / COMPARE_DAYS

        ratio = today_count if avg_count == 0 else today_count / avg_count
        if ratio < ANOMALY_RATIO:
            continue

        items.append({
            "stock_code": code,
            "stock_name": code_to_name.get(code),
            "today_count": today_count,
            "avg_count": round(avg_count, 1),
            "ratio": round(ratio, 1),
        })

    # ratio 내림차순 정렬
    items.sort(key=lambda x: x["ratio"], reverse=True)
    logger.info(f"이상 탐지 완료 - {len(items)}건 감지")
    return items
