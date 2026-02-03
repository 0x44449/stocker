# 뉴스-종목 수동 매핑 테이블 재설계 및 API 구현

## 배경

기존 `company_name_mapping` 테이블은 LLM 추출 결과와 수동 매핑, 검증, 피드백을 하나의 테이블에서 처리하려 했다.
실제로는 LLM 추출 결과는 `news_company_extraction_result`에 이미 있고, 수동 매핑 테이블은 **사람이 검수한 결과를 별도로 기록하는 역할**만 하면 된다.

기존 구조의 문제:
- extracted_name 중복 저장 (extraction_result에 이미 있음)
- match_type, verified 등 학습 파이프라인용 컬럼이 지금 단계에서 불필요
- 피드백이 종목(row) 단위인데, 실제로는 뉴스 단위로 피드백하는 게 자연스러움

## 목표

- `company_name_mapping` 제거
- 단순한 새 테이블 2개로 교체
- admin-web에서 사용할 검수 API 구현

## 관련 테이블

### 기존 (제거 대상)

```sql
-- company_name_mapping (V9, V10)
id, news_id, extracted_name, matched_stock_code, match_type, verified, feedback, created_at, updated_at
```

### 참조할 기존 테이블

```sql
-- news_company_extraction (V6)
id, news_id (UNIQUE), status, created_at, processed_at

-- news_company_extraction_result (V6)
id, extraction_id (FK → news_company_extraction), company_name

-- news_raw (V2)
id, title, raw_text, url, press, source, published_at, collected_at, expires_at

-- stock_master (V8)
isin_code (PK), stock_code (UNIQUE), name_kr, name_kr_short, ...
```

### 새 테이블

```sql
-- 사람이 검수한 뉴스-종목 연결 + 피드백. 뉴스 1건 = 1 row
CREATE TABLE news_stock_manual_mapping (
    id BIGSERIAL PRIMARY KEY,
    news_id BIGINT NOT NULL UNIQUE REFERENCES news_raw(id),
    stock_codes JSONB NOT NULL DEFAULT '[]',
    feedback TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

## 제거할 파일

- `apps/api/src/main/java/com/hanzi/stocker/entities/CompanyNameMappingEntity.java`
- `apps/api/src/main/java/com/hanzi/stocker/repositories/CompanyNameMappingRepository.java`

## 교체할 파일

기존 admin 매핑 코드를 새 테이블 구조에 맞게 전면 교체한다.

- `apps/api/src/main/java/com/hanzi/stocker/api/admin/AdminNewsMappingController.java` → 삭제
- `apps/api/src/main/java/com/hanzi/stocker/api/admin/AdminNewsMappingService.java` → 삭제
- 새 파일은 `api/newsmapping/` 패키지에 생성

## 할 것

### 1. DB Migration

**V11**: `company_name_mapping` 테이블 DROP
**V12**: `news_stock_manual_mapping` 테이블 CREATE

### 2. Entity / Repository

새 테이블에 대한 Entity, Repository 생성.
- 위치: `apps/api/src/main/java/com/hanzi/stocker/api/newsmapping/` 패키지 안에 둔다 (기능 기반 패키지)
- 기존 `entities/`, `repositories/` 패키지의 `CompanyNameMapping` 관련 파일 삭제
- `news_company_extraction`, `news_company_extraction_result` 테이블의 Entity/Repository가 현재 없음. extractedNames 조회를 위해 함께 생성 필요. 위치는 `entities/`, `repositories/` 패키지 (공유 데이터)

### 3. API 구현

**기본 경로**: `/api/news-mappings`

#### 3-1. 뉴스 목록 조회
```
GET /api/news-mappings?filter={filter}&page={page}&size={size}&search={search}
```
- filter: `all` (전체), `reviewed` (검수 완료 - review 존재), `unreviewed` (미검수)
- search: 뉴스 제목 검색

응답:
```json
{
  "items": [
    {
      "news": {
        "newsId": 123,
        "title": "삼성전자 반도체 투자 확대",
        "press": "매일경제",
        "source": "mk",
        "publishedAt": "2026-02-03T10:00:00",
        "collectedAt": "2026-02-03T11:00:00"
      },
      "extractedNames": ["삼성전자", "SK하이닉스"],
      "mapping": {
        "stockCodes": ["005930", "000660"],
        "reviewed": true
      }
    }
  ],
  "totalCount": 1312,
  "page": 0,
  "size": 10
}
```

#### 3-2. 뉴스 상세 조회 (검수 화면용)
```
GET /api/news-mappings/{newsId}
```
응답:
```json
{
  "news": {
    "newsId": 123,
    "title": "삼성전자 반도체 투자 확대",
    "rawText": "삼성전자가 반도체 설비 투자를...",
    "url": "https://...",
    "press": "매일경제",
    "source": "mk",
    "publishedAt": "2026-02-03T10:00:00",
    "collectedAt": "2026-02-03T11:00:00"
  },
  "extractedNames": ["삼성전자", "SK하이닉스"],
  "mapping": {
    "stockCodes": ["005930", "000660"],
    "feedback": "삼성전자 관련 맞음, SK하이닉스는 관련 없음"
  }
}
```

#### 3-3. 종목 연결 + 피드백 저장
```
PUT /api/news-mappings/{newsId}
Body: { "stockCodes": ["005930", "000660"], "feedback": "삼성전자 관련 맞음" }
```
- 내부 동작: upsert (news_id UNIQUE)
- stockCodes가 빈 배열이어도 허용: "검수했지만 관련 종목 없음"을 의미

#### 3-4. 연결 전체 해제
```
DELETE /api/news-mappings/{newsId}
```
- 해당 뉴스의 매핑 row 삭제

#### 3-5. 종목 검색 (매핑 시 종목 찾기용)
```
GET /api/stocks?query={query}
```
- 기존 StockController에 TODO로 있는 검색 API 구현
- stock_master에서 stock_code, name_kr, name_kr_short로 검색
- 검색어 2자 이상에서만 수행, 최대 20건 반환

응답:
```json
[
  {
    "stockCode": "005930",
    "nameKr": "삼성전자",
    "nameKrShort": "삼성전자",
    "market": "KOSPI"
  }
]
```

## 안 할 것

- admin-web 프론트엔드 수정 (별도 작업)
- 자동 매칭 로직 (지금은 수동만)
- extraction_result 수정/삭제 기능

## 코딩 규칙 참고

- 기능 기반 패키지: `api/newsmapping/` 안에 Entity, Repository, Service, Controller 모두 배치
- QueryDSL: 동적 필터 쿼리에 사용, 단순 조회는 JPA Repository
- DB 조회와 비즈니스 로직 분리: DB에서는 ID 조회 + 원본 데이터, 집계는 서비스에서
- 클래스 최소화: Service 하나에 로직 모으기
- springdoc-openapi 3.0.1로 API 문서화

## 완료 조건

- [ ] company_name_mapping 테이블 DROP migration
- [ ] 새 테이블 CREATE migration
- [ ] 기존 CompanyNameMapping Entity/Repository 삭제
- [ ] 새 Entity/Repository 생성 (manual_mapping, extraction, extraction_result)
- [ ] 뉴스 목록 조회 API (필터, 페이징, 검색)
- [ ] 뉴스 상세 조회 API
- [ ] 종목 연결 + 피드백 저장 API (PUT, 전체 갈아끼우기)
- [ ] 연결 전체 해제 API (DELETE)
- [ ] 종목 검색 API
- [ ] Swagger에서 API 동작 확인 가능
