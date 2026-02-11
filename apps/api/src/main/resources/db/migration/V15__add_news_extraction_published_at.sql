-- 컬럼 추가
ALTER TABLE news_extraction ADD COLUMN published_at TIMESTAMP;

-- 기존 데이터: news_raw에서 published_at 가져와서 채우기
UPDATE news_extraction e
SET published_at = n.published_at
FROM news_raw n
WHERE e.news_id = n.id;

-- NOT NULL 제약 추가
ALTER TABLE news_extraction ALTER COLUMN published_at SET NOT NULL;

-- 인덱스 추가
CREATE INDEX idx_news_extraction_published_at ON news_extraction(published_at);
