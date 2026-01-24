CREATE TABLE news_raw (
    id              BIGSERIAL PRIMARY KEY,
    source          VARCHAR(50) NOT NULL,
    press           VARCHAR(100) NOT NULL,
    title           VARCHAR(500) NOT NULL,
    raw_text        VARCHAR(2000) NOT NULL,
    url             VARCHAR(1000) NOT NULL UNIQUE,
    published_at    TIMESTAMP,
    collected_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_news_raw_source ON news_raw(source);
CREATE INDEX idx_news_raw_expires_at ON news_raw(expires_at);
CREATE INDEX idx_news_raw_url ON news_raw(url);
