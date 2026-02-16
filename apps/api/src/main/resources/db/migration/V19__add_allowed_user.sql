CREATE TABLE allowed_user (
    uid TEXT PRIMARY KEY,
    memo TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
