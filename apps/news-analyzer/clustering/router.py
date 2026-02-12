from fastapi import APIRouter, Depends
from pydantic import BaseModel
from sqlalchemy.orm import Session

from clustering.service import cluster_news
from database import get_db

router = APIRouter(prefix="/clustering")


class ClusterRequest(BaseModel):
    keyword: str
    days: int = 2
    eps: float = 0.2


@router.post("/similar-news")
def similar_news(req: ClusterRequest, db: Session = Depends(get_db)):
    return cluster_news(db, req.keyword, req.days, req.eps)
