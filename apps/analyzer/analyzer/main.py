from fastapi import FastAPI

app = FastAPI(title="Stocker Analyzer")


@app.get("/health")
def health():
    return {"status": "ok"}
