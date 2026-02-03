# news_raw 본문 길이 제한 제거

## 배경

뉴스 본문(`raw_text`)이 VARCHAR(2000)으로 제한되어 있어 기사가 중간에 잘린다.
분석 파이프라인(LLM 기업명 추출, 임베딩 등)에서 전체 본문이 필요하므로 제한을 제거한다.

## 목표

`news_raw.raw_text` 컬럼의 길이 제한을 제거하고, 크롤러의 본문 truncation 로직을 제거한다.

## 할 것

### 1. Flyway 마이그레이션 추가 (V13)

`V13__alter_news_raw_text_to_text.sql`:
```sql
ALTER TABLE news_raw ALTER COLUMN raw_text TYPE TEXT;
```

### 2. NewsRawEntity 수정

- `apps/api/src/main/java/com/hanzi/stocker/entities/NewsRawEntity.java`
- `@Column(name = "raw_text", nullable = false, length = 2000)` → `@Column(name = "raw_text", nullable = false, columnDefinition = "TEXT")`

### 3. NewsCrawlEngine truncation 제거

- `apps/api/src/main/java/com/hanzi/stocker/ingest/news/NewsCrawlEngine.java`
- 아래 블록 제거:
```java
if (rawText.length() > config.getRawTextMaxLength()) {
    rawText = rawText.substring(0, config.getRawTextMaxLength());
}
```
- `config.getRawTextMaxLength()` 호출이 없어지므로 관련 코드만 제거. `rawText` 변수는 그대로 사용.

### 4. NewsCrawlConfig에서 rawTextMaxLength 필드 제거

- `apps/api/src/main/java/com/hanzi/stocker/ingest/news/NewsCrawlConfig.java`
- `rawTextMaxLength` 필드, getter, setter 제거

### 5. application.yaml에서 raw-text-max-length 설정 제거

- `apps/api/src/main/resources/application.yaml`
- `crawler.news` 하위의 `raw-text-max-length: 2000` 라인 제거

## 안 할 것

- 기존 데이터 마이그레이션 (이미 잘린 데이터는 그대로 둔다)
