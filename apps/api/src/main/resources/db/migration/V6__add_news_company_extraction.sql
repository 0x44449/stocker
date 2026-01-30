CREATE TABLE news_company_extraction (
    id           BIGSERIAL    PRIMARY KEY,
    news_id      BIGINT       NOT NULL UNIQUE REFERENCES news_raw(id),
    status       VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    processed_at TIMESTAMP
);

CREATE TABLE news_company_extraction_result (
    id            BIGSERIAL    PRIMARY KEY,
    extraction_id BIGINT       NOT NULL REFERENCES news_company_extraction(id),
    company_name  VARCHAR(200) NOT NULL
);
