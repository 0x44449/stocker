import logging
import threading
import time
from datetime import datetime, timezone

from database import SessionLocal
from extraction.service import extract_companies
from models import NewsRaw, NewsCompanyExtraction, NewsCompanyExtractionResult, StockMaster, CompanyNameMapping

logger = logging.getLogger(__name__)

_running = False
_lock = threading.Lock()


def is_running() -> bool:
    return _running


def run_batch():
    global _running

    with _lock:
        if _running:
            return
        _running = True

    try:
        _process_all()
    finally:
        _running = False


def _match_company_to_stock(db, extracted_name: str) -> tuple[str | None, str]:
    """stock_master에서 name_kr 또는 name_kr_short 정확 일치로 종목코드를 찾는다."""
    stock = (
        db.query(StockMaster)
        .filter(
            (StockMaster.name_kr == extracted_name) |
            (StockMaster.name_kr_short == extracted_name)
        )
        .first()
    )
    if stock:
        return (stock.stock_code, "auto_exact")
    return (None, "none")


def _process_all():
    logger.info("배치 시작")
    count = 0
    db = SessionLocal()
    try:
        while True:
            extracted_news_ids = db.query(NewsCompanyExtraction.news_id)
            news = (
                db.query(NewsRaw)
                .filter(NewsRaw.id.notin_(extracted_news_ids))
                .order_by(NewsRaw.id.asc())
                .first()
            )
            if news is None:
                break

            logger.info(f"처리 시작 - news_id={news.id}, title={news.title[:50]}")
            start_time = time.time()

            try:
                companies = extract_companies(news.raw_text)
                extraction = NewsCompanyExtraction(
                    news_id=news.id,
                    status="done",
                    created_at=datetime.now(timezone.utc),
                    processed_at=datetime.now(timezone.utc),
                )
                db.add(extraction)
                db.flush()

                for name in companies:
                    db.add(NewsCompanyExtractionResult(
                        extraction_id=extraction.id,
                        company_name=name,
                    ))

                    # 기업명-종목 자동 매칭 결과 저장
                    stock_code, match_type = _match_company_to_stock(db, name)
                    db.add(CompanyNameMapping(
                        news_id=news.id,
                        extracted_name=name,
                        matched_stock_code=stock_code,
                        match_type=match_type,
                        verified=False,
                        created_at=datetime.now(timezone.utc),
                        updated_at=datetime.now(timezone.utc),
                    ))
                db.commit()

                elapsed = time.time() - start_time
                logger.info(f"처리 완료 - news_id={news.id}, 소요시간: {elapsed:.2f}초, 결과: {companies}")
                count += 1
            except Exception:
                db.rollback()
                elapsed = time.time() - start_time
                extraction = NewsCompanyExtraction(
                    news_id=news.id,
                    status="failed",
                    created_at=datetime.now(timezone.utc),
                )
                db.add(extraction)
                db.commit()
                logger.exception(f"처리 실패 - news_id={news.id}, 소요시간: {elapsed:.2f}초")
    finally:
        db.close()
    logger.info(f"배치 종료 - 총 {count}건 처리")
