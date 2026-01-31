import logging
from contextlib import asynccontextmanager

from apscheduler.schedulers.background import BackgroundScheduler
from fastapi import FastAPI

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)

from extraction.job import run_batch
from extraction.router import router as extraction_router
from embedding.job import run_embedding_batch
from embedding.router import router as embedding_router
from search.router import router as search_router

scheduler = BackgroundScheduler()
scheduler.add_job(run_batch, "cron", hour="9,21", id="batch_extract")
scheduler.add_job(run_embedding_batch, "cron", hour="0,12", id="batch_embedding")


@asynccontextmanager
async def lifespan(app):
    scheduler.start()
    yield
    scheduler.shutdown()


app = FastAPI(title="Stocker News Analyzer", lifespan=lifespan)


@app.get("/health")
def health():
    return {"status": "ok"}


app.include_router(extraction_router)
app.include_router(embedding_router)
app.include_router(search_router)
