CREATE TABLE stock_alias (
    alias TEXT PRIMARY KEY,
    stock_code TEXT NOT NULL,
    stock_name TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_stock_alias_stock_code ON stock_alias(stock_code);
