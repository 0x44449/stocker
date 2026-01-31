import threading

from datetime import datetime, timezone
from fastapi import APIRouter, Depends, Query
from pydantic import BaseModel
from sqlalchemy.orm import Session

from database import get_db
from extraction.service import extract_companies
from extraction.job import run_batch, is_running as is_batch_running
from models import NewsRaw, NewsCompanyExtraction, NewsCompanyExtractionResult

router = APIRouter(prefix="/extraction")


@router.get("/pending")
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


@router.post("/run")
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


@router.post("/job/start")
def batch_start():
    if is_batch_running():
        return {"status": "already_running"}
    threading.Thread(target=run_batch, daemon=True).start()
    return {"status": "started"}


@router.get("/job/status")
def batch_status():
    return {"running": is_batch_running()}
