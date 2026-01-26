-- KRX 일별 전체 지수 시세 원본(raw) 테이블
CREATE TABLE market_index_daily_raw (
    trd_dd DATE NOT NULL,
    index_name TEXT NOT NULL,
    close NUMERIC(18, 2) NULL,
    diff NUMERIC(18, 2) NULL,
    diff_rate NUMERIC(18, 2) NULL,
    open NUMERIC(18, 2) NULL,
    high NUMERIC(18, 2) NULL,
    low NUMERIC(18, 2) NULL,
    volume BIGINT NULL,
    value BIGINT NULL,
    market_cap BIGINT NULL,
    source TEXT NOT NULL DEFAULT 'KRX',
    ingested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (trd_dd, index_name)
);

CREATE INDEX idx_market_index_daily_raw_index_name ON market_index_daily_raw (index_name);
