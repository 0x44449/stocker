CREATE TABLE company_name_mapping (
    id BIGSERIAL PRIMARY KEY,
    news_id BIGINT NOT NULL REFERENCES news_raw(id),
    extracted_name VARCHAR(200) NOT NULL,
    matched_stock_code VARCHAR(20),
    match_type VARCHAR(20) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    feedback TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_company_name_mapping_news_id ON company_name_mapping(news_id);
CREATE INDEX idx_company_name_mapping_match_type ON company_name_mapping(match_type);
CREATE INDEX idx_company_name_mapping_verified ON company_name_mapping(verified);
