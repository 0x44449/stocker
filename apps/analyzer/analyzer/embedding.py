import logging
import threading
from datetime import datetime, timezone

from sentence_transformers import SentenceTransformer

from analyzer.database import SessionLocal
from analyzer.models import NewsRaw, NewsEmbedding

logger = logging.getLogger(__name__)

_running = False
_lock = threading.Lock()

_model = None


def _get_model():
    global _model
    if _model is None:
        _model = SentenceTransformer("nlpai-lab/KURE-v1")
    return _model


def is_running() -> bool:
    return _running


def run_embedding_batch():
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
        model = _get_model()
        while True:
            embedded_news_ids = db.query(NewsEmbedding.news_id)
            news = (
                db.query(NewsRaw)
                .filter(NewsRaw.id.notin_(embedded_news_ids))
                .order_by(NewsRaw.id.asc())
                .first()
            )
            if news is None:
                break

            try:
                text = (news.title or "") + " " + (news.raw_text or "")
                vector = model.encode(text).tolist()

                record = NewsEmbedding(
                    news_id=news.id,
                    embedding=vector,
                    created_at=datetime.now(timezone.utc),
                )
                db.add(record)
                db.commit()
                logger.info(f"Embedded news_id={news.id}")
            except Exception:
                db.rollback()
                logger.exception(f"Failed to embed news_id={news.id}")
    finally:
        db.close()
