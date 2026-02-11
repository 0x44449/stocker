import logging
import threading
import time
from datetime import datetime, timezone

from database import SessionLocal
from extraction.service import extract_companies
from models import NewsRaw, NewsExtraction

logger = logging.getLogger(__name__)

_running = False
_lock = threading.Lock()

LLM_MODEL = "qwen2.5:7b"
PROMPT_VERSION = "v1"


def is_running() -> bool:
    return _running


def run_batch(llm_model: str = LLM_MODEL, prompt_version: str = PROMPT_VERSION):
    global _running

    with _lock:
        if _running:
            return
        _running = True

    try:
        _process_all(llm_model, prompt_version)
    finally:
        _running = False


def _process_all(llm_model: str, prompt_version: str):
    logger.info("배치 시작")
    count = 0
    db = SessionLocal()
    try:
        while True:
            # 동일 모델+프롬프트 조합으로 이미 처리된 뉴스 제외
            extracted_news_ids = (
                db.query(NewsExtraction.news_id)
                .filter(
                    NewsExtraction.llm_model == llm_model,
                    NewsExtraction.prompt_version == prompt_version,
                )
            )
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
                keywords, llm_response = extract_companies(news.raw_text)
                extraction = NewsExtraction(
                    news_id=news.id,
                    keywords=keywords,
                    llm_response=llm_response,
                    llm_model=llm_model,
                    prompt_version=prompt_version,
                    published_at=news.published_at,
                    created_at=datetime.now(timezone.utc),
                )
                db.add(extraction)
                db.commit()

                elapsed = time.time() - start_time
                logger.info(f"처리 완료 - news_id={news.id}, 소요시간: {elapsed:.2f}초, 결과: {keywords}")
                count += 1
            except Exception:
                db.rollback()
                elapsed = time.time() - start_time
                logger.exception(f"처리 실패 - news_id={news.id}, 소요시간: {elapsed:.2f}초")
    finally:
        db.close()
    logger.info(f"배치 종료 - 총 {count}건 처리")
