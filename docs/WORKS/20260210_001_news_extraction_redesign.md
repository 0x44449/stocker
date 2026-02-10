# news_extraction 테이블 재설계

## 배경

기존 LLM 기업명 추출 테이블(`news_company_extraction`, `news_company_extraction_result`)이 설계 문제로 사용이 어렵다.

- 2테이블 분리가 불필요 (extraction은 상태만, result가 실제 데이터)
- extraction_result에서 데이터를 뽑으려면 extraction을 거쳐야 하는 불필요한 JOIN
- job.py에서 이미 삭제된 CompanyNameMapping을 참조하여 배치 에러 발생
- LLM 결과 자체도 만족스럽지 않아 기존 데이터는 신뢰할 수 없음

기존 테이블과 코드는 그대로 두고, 새 테이블과 코드를 추가한다.

## 목표

1. 새 `news_extraction` 테이블 생성
2. Spring Boot에 새 엔티티/리포지토리 추가
3. Python analyzer 코드를 새 테이블 기반으로 수정

## 관련 파일

### DB 마이그레이션
- `apps/api/src/main/resources/db/migration/` 하위에 새 마이그레이션 추가 (V14)

### Spring Boot (새로 추가할 것)
- 새 엔티티: `NewsExtractionEntity`
- 새 리포지토리: `NewsExtractionRepository`

### Spring Boot (건드리지 않는 것)
- `NewsCompanyExtractionEntity`, `NewsCompanyExtractionResultEntity` — HeadlineService가 사용 중이므로 유지
- `NewsCompanyExtractionRepository`, `NewsCompanyExtractionResultRepository` — 유지
- `api/headline/` 패키지 전체 — 유지

### Python Analyzer
- `apps/news-analyzer/extraction/job.py`
- `apps/news-analyzer/extraction/router.py`
- `apps/news-analyzer/extraction/service.py`
- `apps/news-analyzer/models.py` (SQLAlchemy 모델)

## 할 것

### 1. Flyway 마이그레이션 V14 — 새 테이블 생성

```sql
CREATE TABLE news_extraction (
    extraction_id   BIGSERIAL    PRIMARY KEY,
    news_id         BIGINT       NOT NULL,
    keywords        JSONB        NOT NULL DEFAULT '[]',
    llm_response    TEXT,
    llm_model       VARCHAR(100) NOT NULL,
    prompt_version  VARCHAR(50)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_news_extraction_news_id ON news_extraction(news_id);
CREATE INDEX idx_news_extraction_keywords ON news_extraction USING GIN (keywords);
```

컬럼 설명:
- `extraction_id`: PK. 새 네이밍 규칙 적용 (id → 명시적 이름)
- `news_id`: news_raw.id 참조. FK 없음 (CODING_DECISIONS 규칙). UNIQUE 아님 — 같은 뉴스를 다른 모델/프롬프트로 여러 번 추출 가능
- `keywords`: LLM이 뽑은 기업명 리스트. JSONB 배열 (예: `["삼성전자", "SK하이닉스"]`). 기업명이 없는 뉴스는 빈 배열 `[]`
- `llm_response`: LLM 원본 응답 텍스트. 디버깅/비교용
- `llm_model`: 사용한 모델명 (예: `"qwen2.5:7b"`)
- `prompt_version`: 프롬프트 버전 (예: `"v1"`)

### 2. Spring Boot 엔티티/리포지토리 추가

- 새 `NewsExtractionEntity` 생성
  - keywords 컬럼은 JSONB이므로 적절한 JPA 매핑 사용
- 새 `NewsExtractionRepository` 생성
- 기존 `NewsCompanyExtraction*` 엔티티/리포지토리는 그대로 유지

### 3. Python Analyzer 수정

#### models.py
- 새 `NewsExtraction` 모델 추가
- 기존 `NewsCompanyExtraction`, `NewsCompanyExtractionResult`, `CompanyNameMapping` 모델은 그대로 유지

#### extraction/service.py
- `extract_companies()` 함수가 LLM 원본 응답도 함께 반환하도록 수정
- 반환값: `(keywords: list[str], llm_response: str)`

#### extraction/job.py
- 새 `NewsExtraction` 모델 사용하도록 변경
- `CompanyNameMapping` 관련 코드 제거 (이미 테이블 삭제됨, V11. import와 사용 코드만 제거)
- `llm_model`, `prompt_version`, `llm_response` 값 저장
- `_match_company_to_stock()` 함수 제거 (매칭은 이 모듈의 책임이 아님)

#### extraction/router.py
- 새 `NewsExtraction` 모델 사용하도록 변경
- pending 조회: 특정 llm_model + prompt_version 조합으로 아직 처리되지 않은 뉴스를 조회
  - 파라미터: `llm_model`, `prompt_version` (필수), `limit` (선택, 기본값 10)
  - 쿼리: `WHERE NOT EXISTS (SELECT 1 FROM news_extraction e WHERE e.news_id = n.id AND e.llm_model = :model AND e.prompt_version = :version)`
- 배치(job) 실행 시에도 llm_model, prompt_version을 파라미터로 받아서 동일한 기준으로 처리
- 배치는 1건씩 순차 처리 (기존 방식 유지)

## 안 할 것

- 기존 테이블 (`news_company_extraction`, `news_company_extraction_result`) 변경/삭제
- 기존 Spring Boot 엔티티/리포지토리 변경/삭제
- HeadlineService/HeadlineController 수정
- LLM 모델 변경 / 프롬프트 개선 (별도 작업)
- news_stock_link 등 매칭 결과 테이블 (별도 작업)

## 참고

- CODING_DECISIONS.md: FK 사용하지 않음, PK 네이밍 명시적으로
