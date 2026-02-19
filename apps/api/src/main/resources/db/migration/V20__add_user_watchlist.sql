CREATE TABLE user_watchlist (
    user_id TEXT NOT NULL,
    stock_code TEXT NOT NULL,
    sort_order INT NOT NULL,
    added_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, stock_code)
);

CREATE INDEX idx_user_watchlist_user_id ON user_watchlist(user_id);
