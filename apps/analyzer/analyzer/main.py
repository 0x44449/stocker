from datetime import datetime, timezone

from fastapi import Depends, FastAPI, Query
from pydantic import BaseModel
from sqlalchemy.orm import Session

from analyzer.database import get_db
from analyzer.llm import extract_companies
from analyzer.models import NewsRaw, NewsCompanyExtraction, NewsCompanyExtractionResult

app = FastAPI(title="Stocker Analyzer")


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
