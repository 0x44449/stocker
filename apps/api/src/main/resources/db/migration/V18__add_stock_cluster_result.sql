CREATE TABLE stock_cluster_result (
    stock_code TEXT NOT NULL,
    stock_name TEXT NOT NULL,
    total_count INT NOT NULL,
    input_hash TEXT NOT NULL,
    clustered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    result JSONB NOT NULL,
    PRIMARY KEY (stock_code, clustered_at)
);
