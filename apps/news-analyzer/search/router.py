from fastapi import APIRouter, Depends, Query
from sqlalchemy import text
from sqlalchemy.orm import Session

from database import get_db
from embedding.service import get_model

router = APIRouter()


@router.get("/search")
def search(q: str = Query(), limit: int = Query(default=10), db: Session = Depends(get_db)):
    model = get_model()
    query_vector = model.encode(q).tolist()

    sql = text("""
        SELECT e.news_id, n.title, e.embedding <=> :query_vector AS distance
        FROM news_embedding e
        JOIN news_raw n ON e.news_id = n.id
        ORDER BY distance
        LIMIT :limit
    """)
    rows = db.execute(sql, {"query_vector": str(query_vector), "limit": limit}).fetchall()
    return {
        "results": [
            {"news_id": row[0], "title": row[1], "distance": round(row[2], 4)}
            for row in rows
        ]
    }
