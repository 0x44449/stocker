# syntax=docker/dockerfile:1
# Build stage
FROM python:3.12-slim AS build
WORKDIR /app
RUN --mount=type=cache,target=/root/.cache/pip \
    pip install poetry
COPY pyproject.toml poetry.lock* ./
RUN --mount=type=cache,target=/root/.cache/pypoetry \
    poetry config virtualenvs.create false \
    && poetry install --only main --no-interaction --no-ansi --no-root

# Runtime stage
FROM python:3.12-slim
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r appgroup && useradd -r -g appgroup appuser

# 빌드 스테이지에서 설치된 패키지 복사
COPY --from=build /usr/local/lib/python3.12/site-packages /usr/local/lib/python3.12/site-packages
COPY --from=build /usr/local/bin /usr/local/bin

# Huggingface 모델 캐시 디렉토리
ENV HF_HOME=/app/.cache

COPY main.py config.py database.py models.py ./
COPY extraction extraction
COPY embedding embedding
COPY search search
COPY clustering clustering
RUN mkdir -p /app/.cache && chown -R appuser:appgroup /app
USER appuser
EXPOSE 8000
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
