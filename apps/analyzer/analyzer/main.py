from fastapi import Depends, FastAPI, Query
from sqlalchemy.orm import Session

from analyzer.database import get_db
from analyzer.models import NewsRaw, NewsCompanyExtraction

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
