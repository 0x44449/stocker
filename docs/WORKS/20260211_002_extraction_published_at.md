# news_extraction에 published_at 컬럼 추가

## 배경

현재 news_extraction에 뉴스 발행일이 없어서, 날짜 기준 조회 시 news_raw를 JOIN해야 한다.
published_at을 news_extraction에 추가하여 단독 조회 가능하게 한다.

## 목표

1. Flyway V15 마이그레이션으로 published_at 컬럼 추가 + 기존 데이터 채우기
2. Spring Boot 엔티티 수정
3. Python 모델/코드 수정

## 관련 테이블

```
news_extraction   - extraction_id, news_id, keywords, llm_model, prompt_version, created_at
news_raw          - id, published_at
```

## 할 것

### 1. Flyway V15 마이그레이션

```sql
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
```

### 2. Spring Boot

- `NewsExtractionEntity`에 `publishedAt` 필드 추가

### 3. Python Analyzer

- `models.py`의 `NewsExtraction`에 `published_at` 컬럼 추가
- `extraction/job.py`에서 extraction 저장 시 news_raw의 published_at을 함께 저장

### 4. HotStockService 수정

- `created_at` 기준 조회를 `published_at` 기준으로 변경

## 안 할 것

- HeadlineService 수정
- news_raw 테이블 변경
