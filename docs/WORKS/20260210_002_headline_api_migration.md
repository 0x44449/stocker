# 헤드라인 API를 news_extraction 기반으로 수정

## 배경

HeadlineService가 기존 `news_company_extraction` + `news_company_extraction_result` 테이블을 사용 중이다.
새로 만든 `news_extraction` 테이블 기반으로 변경한다.

## 목표

HeadlineService/Controller를 `news_extraction` 테이블 기반으로 수정한다.

## 관련 파일

- `apps/api/src/main/java/com/hanzi/stocker/api/headline/HeadlineService.java`
- `apps/api/src/main/java/com/hanzi/stocker/api/headline/HeadlineController.java`
- `apps/api/src/main/java/com/hanzi/stocker/entities/NewsExtractionEntity.java`

## 관련 테이블

```
news_raw
  - id, title, published_at, ...

news_extraction
  - extraction_id (PK), news_id, keywords (JSONB 배열), llm_model, prompt_version, created_at
```

## 할 것

### 1. HeadlineService 수정

- 기존 `NewsCompanyExtractionEntity`, `NewsCompanyExtractionResultEntity`, `NewsCompanyExtractionResultRepository` 의존 제거
- `NewsExtractionEntity` 기반으로 변경

로직:
1. news_raw에서 해당 date의 뉴스 조회
2. news_extraction에서 해당 뉴스들의 추출 결과 조회
   - llm_model, prompt_version은 하드코딩 (`"qwen2.5:7b"`, `"v1"`)
3. keywords JSONB 배열을 풀어서 기업명별 카운트
4. threshold 이상인 종목만 선정, count 내림차순

응답 형태는 기존과 동일:
```json
{
  "date": "2026-02-05",
  "threshold": 5,
  "totalNewsCount": 150,
  "headlines": [
    {
      "companyName": "삼성전자",
      "count": 23,
      "articles": [...]
    }
  ]
}
```

### 2. HeadlineController

- 변경 없음 (파라미터, 응답 형태 동일)

## 안 할 것

- llm_model, prompt_version 파라미터화 (하드코딩)
- 기존 extraction 엔티티/리포지토리 삭제
- 페이징
