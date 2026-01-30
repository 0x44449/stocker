CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE news_embedding (
    id           BIGSERIAL PRIMARY KEY,
    news_id      BIGINT NOT NULL UNIQUE REFERENCES news_raw(id),
    embedding    vector(1024) NOT NULL,
    created_at   TIMESTAMP NOT NULL
);

CREATE INDEX idx_news_embedding_vector ON news_embedding USING ivfflat (embedding vector_cosine_ops);
