import hashlib
import logging
import threading
from datetime import datetime, date, timedelta, timezone

from config import LLM_MODEL, PROMPT_VERSION
from database import SessionLocal
from clustering.service import cluster_news
from models import (
    NewsExtraction, StockMaster, StockAlias, SubsidiaryMapping, StockClusterResult,
)

logger = logging.getLogger(__name__)

_running = False
_lock = threading.Lock()


def is_running() -> bool:
    return _running


def run_clustering_batch():
    global _running

    with _lock:
        if _running:
            return
        _running = True

    try:
        _process_all()
    finally:
        _running = False


def _process_all():
    logger.info("클러스터링 배치 시작")
    db = SessionLocal()
    try:
        start = datetime.combine(date.today() - timedelta(days=1), datetime.min.time())

        # 최근 24시간 extraction에서 멘션된 keywords 수집
        extractions = (
            db.query(NewsExtraction.keywords)
            .filter(
                NewsExtraction.published_at >= start,
                NewsExtraction.llm_model == LLM_MODEL,
                NewsExtraction.prompt_version == PROMPT_VERSION,
            )
            .all()
        )
        all_keywords = set()
        for row in extractions:
            for kw in row.keywords or []:
                all_keywords.add(kw)

        logger.info(f"수집된 키워드 수: {len(all_keywords)}")

        # keyword → stock_code 정규화를 위한 매핑 구성
        name_map = {r.name_kr_short: r.stock_code for r in db.query(StockMaster.name_kr_short, StockMaster.stock_code).all()}
        alias_map = {r.alias: (r.stock_code, r.stock_name) for r in db.query(StockAlias).all()}
        sub_map = {r.subsidiary_name: (r.stock_code, r.stock_name) for r in db.query(SubsidiaryMapping).all()}

        # keyword → (stock_code, stock_name) 매핑
        stock_targets = {}
        for kw in all_keywords:
            if kw in name_map:
                code = name_map[kw]
                stock_targets[code] = kw  # stock_name = keyword 자체
            elif kw in alias_map:
                code, name = alias_map[kw]
                stock_targets.setdefault(code, name)
            elif kw in sub_map:
                code, name = sub_map[kw]
                stock_targets.setdefault(code, name)

        logger.info(f"클러스터링 대상 종목 수: {len(stock_targets)}")

        count = 0
        for stock_code, stock_name in stock_targets.items():
            try:
                _process_stock(db, stock_code, stock_name, name_map, alias_map, sub_map, start)
                count += 1
            except Exception:
                db.rollback()
                logger.exception(f"종목 처리 실패 - {stock_name}({stock_code})")

        logger.info(f"클러스터링 배치 종료 - {count}/{len(stock_targets)}건 처리")
    finally:
        db.close()


def _process_stock(db, stock_code, stock_name, name_map, alias_map, sub_map, start):
    """종목 1건에 대해 클러스터링 수행 및 결과 저장"""
    from sqlalchemy import or_

    # 검색 키워드 구성 (종목명 + aliases + subsidiaries)
    aliases = [a for a, (c, _) in alias_map.items() if c == stock_code]
    subsidiaries = [s for s, (c, _) in sub_map.items() if c == stock_code]
    search_keywords = [stock_name] + aliases + subsidiaries

    # 해당 키워드가 포함된 news_id 조회
    news_ids = (
        db.query(NewsExtraction.news_id)
        .filter(
            or_(*[NewsExtraction.keywords.op("@>")(f'["{kw}"]') for kw in search_keywords]),
            NewsExtraction.published_at >= start,
            NewsExtraction.llm_model == LLM_MODEL,
            NewsExtraction.prompt_version == PROMPT_VERSION,
        )
        .order_by(NewsExtraction.news_id.asc())
        .all()
    )
    sorted_ids = [str(r.news_id) for r in news_ids]

    if not sorted_ids:
        return

    # input_hash 계산
    input_hash = hashlib.md5(",".join(sorted_ids).encode()).hexdigest()

    # 기존 최신 결과와 hash 비교
    latest = (
        db.query(StockClusterResult)
        .filter(StockClusterResult.stock_code == stock_code)
        .order_by(StockClusterResult.clustered_at.desc())
        .first()
    )
    if latest and latest.input_hash == input_hash:
        logger.info(f"스킵 - {stock_name}({stock_code}): input 변경 없음")
        return

    # 클러스터링 실행 (LLM 요약 포함)
    logger.info(f"클러스터링 실행 - {stock_name}({stock_code}), 뉴스 {len(sorted_ids)}건")
    result = cluster_news(db, stock_name, 2, 0.2)

    # 결과 저장
    row = StockClusterResult(
        stock_code=stock_code,
        stock_name=stock_name,
        total_count=result.get("total_count", 0),
        input_hash=input_hash,
        clustered_at=datetime.now(timezone.utc),
        result=result,
    )
    db.add(row)
    db.commit()
    logger.info(f"저장 완료 - {stock_name}({stock_code}), total_count={result.get('total_count', 0)}")
