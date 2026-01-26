-- KRX 투자자별 거래실적 원본(raw) 테이블
CREATE TABLE investor_flow_daily_raw (
    trd_dd DATE NOT NULL,
    market TEXT NOT NULL,
    investor_name TEXT NOT NULL,
    sell_volume BIGINT NULL,
    buy_volume BIGINT NULL,
    net_volume BIGINT NULL,
    sell_value BIGINT NULL,
    buy_value BIGINT NULL,
    net_value BIGINT NULL,
    source TEXT NOT NULL DEFAULT 'KRX',
    ingested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (trd_dd, market, investor_name)
);
