CREATE TABLE stock_master (
    isin_code TEXT PRIMARY KEY,
    stock_code TEXT NOT NULL UNIQUE,
    name_kr TEXT NOT NULL,
    name_kr_short TEXT NOT NULL,
    name_en TEXT,
    listed_date DATE,
    market TEXT NOT NULL,
    security_type TEXT,
    department TEXT,
    stock_type TEXT,
    face_value BIGINT,
    listed_shares BIGINT,
    ingested_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_stock_master_stock_code ON stock_master(stock_code);
CREATE INDEX idx_stock_master_name_kr_short ON stock_master(name_kr_short);
