import logging
import threading
from datetime import datetime, timezone

from database import SessionLocal
from extraction.service import extract_companies
from models import NewsRaw, NewsCompanyExtraction, NewsCompanyExtractionResult

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


def _process_all():
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
                db.commit()
                logger.info(f"Extracted news_id={news.id}: {companies}")
            except Exception:
                db.rollback()
                extraction = NewsCompanyExtraction(
                    news_id=news.id,
                    status="failed",
                    created_at=datetime.now(timezone.utc),
                )
                db.add(extraction)
                db.commit()
                logger.exception(f"Failed to process news_id={news.id}")
    finally:
        db.close()
