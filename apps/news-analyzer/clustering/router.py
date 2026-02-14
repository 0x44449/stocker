import threading

from fastapi import APIRouter, Depends
from pydantic import BaseModel
from sqlalchemy.orm import Session

from clustering.job import run_clustering_batch, is_running as is_clustering_running
from clustering.service import cluster_news
from database import get_db

router = APIRouter(prefix="/clustering")


class ClusterRequest(BaseModel):
    keyword: str
    days: int = 2
    eps: float = 0.2


class ArticleDto(BaseModel):
    news_id: int
    title: str


class StockPriceDto(BaseModel):
    stock_code: str
    date: str
    close: int | None
    diff_rate: float | None


class RelatedStockDto(BaseModel):
    stock_name: str | None
    stock_code: str
    mention_count: int
    close: int | None
    diff_rate: float | None


class TopicDto(BaseModel):
    title: str
    summary: str | None
    count: int
    articles: list[ArticleDto]


class ClusterDto(BaseModel):
    count: int
    articles: list[ArticleDto]


class ClusterResponse(BaseModel):
    keyword: str
    total_count: int
    stock_price: StockPriceDto | None
    related_stock: RelatedStockDto | None
    topic: TopicDto | None
    clusters: list[ClusterDto]
    noise: list[ArticleDto]


@router.post("/job/start")
def clustering_job_start():
    if is_clustering_running():
        return {"status": "already_running"}
    threading.Thread(target=run_clustering_batch, daemon=True).start()
    return {"status": "started"}


@router.get("/job/status")
def clustering_job_status():
    return {"running": is_clustering_running()}


@router.post("/similar-news", response_model=ClusterResponse)
def similar_news(req: ClusterRequest, db: Session = Depends(get_db)):
    return cluster_news(db, req.keyword, req.days, req.eps)
