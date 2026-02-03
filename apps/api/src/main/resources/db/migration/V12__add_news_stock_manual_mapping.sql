-- 사람이 검수한 뉴스-종목 연결 + 피드백. 뉴스 1건 = 1 row
CREATE TABLE news_stock_manual_mapping (
    id BIGSERIAL PRIMARY KEY,
    news_id BIGINT NOT NULL UNIQUE REFERENCES news_raw(id),
    stock_codes JSONB NOT NULL DEFAULT '[]',
    feedback TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
