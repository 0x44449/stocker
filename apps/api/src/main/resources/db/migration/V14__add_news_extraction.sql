CREATE TABLE news_extraction (
    extraction_id   BIGSERIAL    PRIMARY KEY,
    news_id         BIGINT       NOT NULL,
    keywords        JSONB        NOT NULL DEFAULT '[]',
    llm_response    TEXT,
    llm_model       VARCHAR(100) NOT NULL,
    prompt_version  VARCHAR(50)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_news_extraction_news_id ON news_extraction(news_id);
CREATE INDEX idx_news_extraction_keywords ON news_extraction USING GIN (keywords);
