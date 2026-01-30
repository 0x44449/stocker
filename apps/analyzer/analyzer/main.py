import threading
from contextlib import asynccontextmanager
from datetime import datetime, timezone

from apscheduler.schedulers.background import BackgroundScheduler
from fastapi import Depends, FastAPI, Query
from pydantic import BaseModel
from sqlalchemy.orm import Session

from analyzer.batch import run_batch, is_running as is_batch_running
from analyzer.embedding import run_embedding_batch, is_running as is_embedding_running
from analyzer.database import get_db
from analyzer.llm import extract_companies
from analyzer.models import NewsRaw, NewsCompanyExtraction, NewsCompanyExtractionResult

scheduler = BackgroundScheduler()
scheduler.add_job(run_batch, "cron", hour="9,21", id="batch_extract")
scheduler.add_job(run_embedding_batch, "cron", hour="0,12", id="batch_embedding")


@asynccontextmanager
async def lifespan(app):
    scheduler.start()
    yield
    scheduler.shutdown()


app = FastAPI(title="Stocker Analyzer", lifespan=lifespan)


@app.get("/health")
def health():
    return {"status": "ok"}


@app.get("/pending-news")
def pending_news(limit: int = Query(default=10), db: Session = Depends(get_db)):
    extracted_news_ids = db.query(NewsCompanyExtraction.news_id)
    rows = (
        db.query(NewsRaw.id, NewsRaw.title)
        .filter(NewsRaw.id.notin_(extracted_news_ids))
        .limit(limit)
        .all()
    )
    return [{"id": row.id, "title": row.title} for row in rows]


class ExtractRequest(BaseModel):
    news_id: int


@app.post("/extract")
def extract(req: ExtractRequest, db: Session = Depends(get_db)):
    news = db.query(NewsRaw).filter(NewsRaw.id == req.news_id).one()

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
        result = NewsCompanyExtractionResult(
            extraction_id=extraction.id,
            company_name=name,
        )
        db.add(result)

    db.commit()
    return {"news_id": news.id, "companies": companies}


@app.post("/batch/start")
def batch_start():
    if is_batch_running():
        return {"status": "already_running"}
    threading.Thread(target=run_batch, daemon=True).start()
    return {"status": "started"}


@app.get("/batch/status")
def batch_status():
    return {"running": is_batch_running()}


@app.post("/embedding/start")
def embedding_start():
    if is_embedding_running():
        return {"status": "already_running"}
    threading.Thread(target=run_embedding_batch, daemon=True).start()
    return {"status": "started"}


@app.get("/embedding/status")
def embedding_status():
    return {"running": is_embedding_running()}
