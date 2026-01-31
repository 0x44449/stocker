import threading

from fastapi import APIRouter

from embedding.job import run_embedding_batch, is_running as is_embedding_running

router = APIRouter(prefix="/embedding")


@router.post("/job/start")
def embedding_start():
    if is_embedding_running():
        return {"status": "already_running"}
    threading.Thread(target=run_embedding_batch, daemon=True).start()
    return {"status": "started"}


@router.get("/job/status")
def embedding_status():
    return {"running": is_embedding_running()}
