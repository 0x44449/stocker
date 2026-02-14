CREATE TABLE subsidiary_mapping (
    subsidiary_name TEXT PRIMARY KEY,
    stock_code      TEXT NOT NULL,
    stock_name      TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_subsidiary_mapping_stock_code ON subsidiary_mapping (stock_code);
