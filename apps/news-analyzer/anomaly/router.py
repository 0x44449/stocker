from fastapi import APIRouter, Depends
from pydantic import BaseModel
from sqlalchemy.orm import Session

from anomaly.service import detect_anomalies
from database import get_db

router = APIRouter(prefix="/anomaly")


class AnomalyItem(BaseModel):
    stock_code: str
    stock_name: str | None
    today_count: int
    avg_count: float
    ratio: float


class AnomalyResponse(BaseModel):
    items: list[AnomalyItem]


@router.get("/detect", response_model=AnomalyResponse)
def detect(db: Session = Depends(get_db)):
    items = detect_anomalies(db)
    return {"items": items}
