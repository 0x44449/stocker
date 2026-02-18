import threading

from datetime import datetime, timezone
from fastapi import APIRouter, Depends, Query
from pydantic import BaseModel
from sqlalchemy.orm import Session

from database import get_db
from extraction.service import extract_companies
from config import LLM_MODEL, PROMPT_VERSION
from extraction.job import run_batch, is_running as is_batch_running
from models import NewsRaw, NewsExtraction

router = APIRouter(prefix="/extraction")


@router.get("/pending")
def pending_news(
    llm_model: str = Query(),
    prompt_version: str = Query(),
    limit: int = Query(default=10),
    db: Session = Depends(get_db),
):
    extracted_news_ids = (
        db.query(NewsExtraction.news_id)
        .filter(
            NewsExtraction.llm_model == llm_model,
            NewsExtraction.prompt_version == prompt_version,
        )
    )
    rows = (
        db.query(NewsRaw.id, NewsRaw.title)
        .filter(NewsRaw.id.notin_(extracted_news_ids))
        .limit(limit)
        .all()
    )
    return [{"id": row.id, "title": row.title} for row in rows]


class ExtractRequest(BaseModel):
    news_id: int
    llm_model: str = LLM_MODEL
    prompt_version: str = PROMPT_VERSION


@router.post("/run")
def extract(req: ExtractRequest, db: Session = Depends(get_db)):
    news = db.query(NewsRaw).filter(NewsRaw.id == req.news_id).one()

    keywords, llm_response = extract_companies(news.raw_text)

    extraction = NewsExtraction(
        news_id=news.id,
        keywords=keywords,
        llm_response=llm_response,
        llm_model=req.llm_model,
        prompt_version=req.prompt_version,
        published_at=news.published_at,
        created_at=datetime.now(timezone.utc),
    )
    db.add(extraction)
    db.commit()
    return {"news_id": news.id, "keywords": keywords}


class BatchStartRequest(BaseModel):
    llm_model: str = LLM_MODEL
    prompt_version: str = PROMPT_VERSION


@router.post("/job/start")
def batch_start(req: BatchStartRequest = BatchStartRequest()):
    if is_batch_running():
        return {"status": "already_running"}
    threading.Thread(target=run_batch, args=(req.llm_model, req.prompt_version), daemon=True).start()
    return {"status": "started"}


@router.get("/job/status")
def batch_status():
    return {"running": is_batch_running()}
