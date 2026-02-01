# AI 주식 상황 요약·분석 데이터 정제 서비스 (Spring Boot + PostgreSQL) 통합 정리

> 목적: 개인 프로젝트 MVP를 빠르게 만들기 위해 필요한 **프로젝트 기획 + 개발 작업 리스트 + Spring Boot 구성 + 아키텍처/DB/배치 설계**를 한 파일로 통합 정리한다.

---

## 0) 프로젝트 한 줄 소개

주식의 시세/공시/뉴스를 **AI가 이해하기 좋은 형태로 정제**해서, 사용자가 “지금 상황이 어떤지”를 빠르게 파악할 수 있게 돕는 서비스.

- 예측/매수추천이 아니라 **상황 요약/근거/체크리스트** 제공
- 핵심 경쟁력: **LLM이 헛소리하지 않도록 ‘팩트 카드’ 형태로 데이터 정제**

---

## 1) 문제 정의 (왜 필요한가)

주식 정보는 많지만 개인이 매일 확인하기엔 너무 분산되어 있다.

- 시세는 시세대로 있고
- 공시는 공시대로 따로 있고
- 뉴스는 많지만 중복/노이즈가 많음
- 결국 “왜 올랐는지 / 지금 위험한지 / 뭘 체크해야 하는지”를 스스로 정리해야 함

**핵심 문제:** 정보가 부족한 게 아니라 **상황 파악이 어렵다**.

---

## 2) 해결 방법 (무엇을 제공하나)

사용자가 종목을 선택하면 다음을 제공한다.

1) **오늘의 상황 요약 (3줄)**  
2) **근거 데이터(팩트) 카드**  
   - 최근 수익률(1D/1W/1M)  
   - 거래량/거래대금 변화  
   - 공시 이벤트 요약  
   - 뉴스 요약(최소량)  
3) **리스크/호재 체크리스트**  
4) **다음에 확인해야 할 항목 추천**  

---

## 3) 핵심 차별점

LLM을 그냥 쓰는 것이 아니라,

- 원천 데이터(시세/공시/뉴스)를
- AI가 이해하기 쉬운 형태로 **구조화/정제**
- 모델이 답변할 때 **근거 기반 출력**을 강제

즉, “AI 분석”이 아니라 **AI가 잘 답하게 만드는 데이터 계층**을 만든다.

---

## 4) 수집할 데이터(비용 최소 MVP)

개인 프로젝트이므로 “많이” 모으지 않고, “핵심만” 모은다.

### 4.1 필수 1순위: 가격/거래량(일봉)
- OHLCV (시가/고가/저가/종가/거래량)
- 거래대금(= 종가 * 거래량)
- 1D / 5D / 20D 수익률
- 20일 평균 거래대금 대비 오늘 거래대금 비율(급증 감지)

### 4.2 필수 2순위: 공시(팩트 기반)
- DART 기반 공시
- 실적/유증/합병/계약/최대주주 변경 등 “영향 큰 이벤트” 중심

### 4.3 선택 3순위: 뉴스(소량만)
- 최근 24~72시간
- 상위 5~10개만
- 제목 중복 제거
- 제목 + 2~3문장 요약 + 출처 + 시간

---

## 5) MVP 화면 구성(예시)

### 5.1 메인
- 관심종목 리스트
- 종목 검색창
- 오늘 업데이트 시간 표시

### 5.2 종목 상세
- 오늘 요약 3줄
- 최근 수익률/거래대금 변화 카드
- 공시 이벤트 리스트
- 체크리스트(리스크/호재)
- (선택) 가격 차트(라인)

---

## 6) 개발 작업 리스트 (우선순위)

### 6.1 프로젝트 기본 세팅
- [ ] 저장소 생성 (server / worker / web 분리 or monorepo)
- [ ] 환경변수 정리 (.env: DB, API KEY, LLM KEY)
- [ ] Docker Compose (Postgres)

### 6.2 데이터 모델(DB) 설계 (최소)
- [ ] stock (종목코드, 종목명, 시장)
- [ ] daily_price (date, code, open/high/low/close, volume, value)
- [ ] disclosure (dateTime, code, title, type, url, rawJson)
- [ ] watchlist (userId, code)
- [ ] 인덱스: (code, date), (code, dateTime)

### 6.3 데이터 수집 파이프라인
- [ ] 종목 리스트 수집 + 저장
- [ ] 일봉 수집 API 선정(무료) + 최근 2년치 백필
- [ ] 일봉 매일 업데이트 job
- [ ] DART API KEY 발급/연동
- [ ] 주요 공시 타입 필터링 규칙 정의
- [ ] 공시 업데이트 job

### 6.4 정제(팩트 카드) 생성
- [ ] 수익률 계산: 1D/5D/20D
- [ ] 거래대금 계산: close * volume
- [ ] 평균 대비 거래대금 배수 계산
- [ ] 공시 이벤트 type 매핑
- [ ] 룰 기반 레이블 생성(급등/급락/거래 터짐 등)

### 6.5 LLM 요약 생성(선택)
- [ ] 프롬프트 템플릿 작성(팩트 기반, 예측 금지)
- [ ] 출력 포맷 JSON 고정
- [ ] 하루 1회 캐싱(summary_cache)

### 6.6 API 서버 구현
- [ ] GET /api/stocks?query=
- [ ] GET /api/stocks/{code}/situation?date=
- [ ] GET /api/stocks/{code}/prices?from=&to=
- [ ] GET /api/stocks/{code}/disclosures?from=&to=
- [ ] GET /api/watchlist
- [ ] POST /api/watchlist/{code}
- [ ] DELETE /api/watchlist/{code}

### 6.7 웹 UI(단순)
- [ ] 관심종목 리스트 + 검색
- [ ] 종목 상세(요약/근거/공시)

### 6.8 배치/운영
- [ ] 스케줄링(장 마감 후 일봉 업데이트)
- [ ] 공시 업데이트(1시간마다 등)
- [ ] 실패 재시도/로그

### 6.9 품질/안전장치
- [ ] “투자 조언 아님” 고지 문구
- [ ] 데이터 부족 시 안전 처리
- [ ] LLM 출력 JSON 검증 실패 대응

---

## 7) 아키텍처 개요 (MVP 권장)

### 모놀리식 Spring Boot + 역할 분리
MVP 단계에서는 마이크로서비스보다 아래 구조가 현실적이다.

- **API 서버 (REST)**
  - 종목 검색
  - 관심종목
  - 종목 상황 요약 조회

- **Ingest Job (수집 배치)**
  - 일봉 수집
  - DART 공시 수집
  - 스케줄 기반 자동 업데이트

- **Normalizer/Analysis (정제)**
  - 수익률/거래대금/이상치 계산
  - “팩트 카드” 생성
  - (선택) LLM 요약 생성 + 캐싱

> 처음부터 분산/큐/이벤트 기반으로 가면 개발 속도가 떨어지고 운영이 어려워짐.

### 설계 원칙
- `api`: 컨트롤러 + 요청/응답 DTO만 (비즈니스 로직 금지)
- `domain`: 핵심 비즈니스 로직/정제 로직
- `ingest`: 외부 데이터 수집 및 저장(배치)
- `infra`: 외부 연동(WebClient/LLM/설정)
- `common`: 예외/유틸/로깅 등 공통

---

## 8) Spring Boot 백엔드 구조 제안 (Java, Lombok 제외)

### 8.1 패키지 구조 예시
```
com.yourapp.stockai
  ├─ api
  │   ├─ StockController
  │   ├─ WatchlistController
  │   └─ dto
  ├─ domain
  │   ├─ stock
  │   ├─ price
  │   ├─ disclosure
  │   └─ summary
  ├─ ingest
  │   ├─ price
  │   └─ dart
  ├─ infra
  │   ├─ db
  │   ├─ http
  │   ├─ llm
  │   └─ clock
  └─ common
      ├─ config
      ├─ exception
      └─ util
```

---

## 9) 데이터베이스 모델(최소) + 인덱스

> 개인 프로젝트 MVP에 필요한 최소 테이블

### 9.1 stock
- `code` (PK, 종목코드)
- `name` (종목명)
- `market` (KOSPI/KOSDAQ/ETC)
- `isActive` (선택)

**Index**
- `(name)` 검색용 인덱스(또는 trigram/FTS는 나중)

### 9.2 daily_price
- (PK) `code + date`
- `open`, `high`, `low`, `close`
- `volume`
- `value` (거래대금 = close * volume)

**Index**
- `(code, date)` 필수

### 9.3 disclosure
- (PK) `id` (DART 원본 키가 있으면 그것 사용, 없으면 UUID)
- `code`
- `disclosedAt` (datetime)
- `title`
- `type` (내부 분류)
- `url`
- `rawJson` (원본 저장; PostgreSQL이면 JSONB 권장)

**Index**
- `(code, disclosedAt)`
- `(type, disclosedAt)` (선택)

### 9.4 watchlist
> 로그인 없이 단일 사용자로 시작해도 되지만, 구조는 만들어두는 게 좋음

- (PK) `userId + code`
- `createdAt`

**Index**
- `(userId)`

### 9.5 summary_cache (선택이지만 강력 추천)
> LLM 비용/속도 안정화를 위해 “하루 1회” 캐싱

- (PK) `code + date`
- `resultJson`
- `createdAt`

**Index**
- `(date)`

---

## 10) 핵심 도메인: SituationCard(팩트 카드)

> LLM에게 원천 데이터를 그대로 주지 말고 **정제된 팩트 카드**를 만든다.

### SituationCard 예시(JSON 컨셉)
- `asOfDate`
- `priceState`
  - `close`
  - `return1D`, `return5D`, `return20D`
  - `valueToday`, `valueAvg20D`, `valueSpikeRatio`
- `events`
  - 공시 목록(최근 N개)
- `labels`
  - `PRICE_SPIKE`, `VOLUME_SPIKE`, `DISCLOSURE_EXISTS` 등

---

## 11) 핵심 서비스: SummaryService 중심 설계

### SummaryService.getSituation(code, date)
**이 프로젝트의 중심 API/로직**

1. 가격 데이터 로드(최근 20~60일)
2. 수익률/거래대금/이상치 계산
3. 공시 이벤트 로드(최근 N개)
4. SituationCard 생성
5. (선택) LLM 요약 생성 or 캐시 반환
6. 응답 DTO로 변환

> “한 번의 호출로 사용자에게 보여줄 화면이 완성되게” 만드는 것이 개발 속도가 빠름.

---

## 12) 배치/스케줄링 설계

### 12.1 PriceIngestJob
- 목적: 종목별 일봉 업데이트
- 실행: **매일 장 마감 이후 1회**
- 처리:
  - 신규 일자만 upsert
  - 결측/휴장일은 skip

### 12.2 DartIngestJob
- 목적: 종목별 공시 업데이트
- 실행: **1시간마다** (또는 30분)
- 처리:
  - 주요 공시만 필터링
  - 중복 제거(upsert)

### 12.3 SummaryWarmupJob (선택)
- 목적: 관심종목 요약 미리 생성(응답 속도 개선)
- 실행: 장 마감 후 또는 새벽 1회

---

## 13) 외부 연동(HTTP) 방식

### WebClient 권장
- 타임아웃 설정 필수
- Retry는 제한적으로
- rate limit 대응(백오프/슬립)

> RestTemplate은 신규 프로젝트에서는 비추천.

---

## 14) Spring Boot 구성 정책 (Actuator / Validation / Flyway)

### 14.1 현재 결정사항
- Lombok: 초기 제외
- Validation: 초기부터 도입
- Flyway: 초기부터 도입
- Actuator: 목록만 유지, 나중에 필요하면 추가

---

## 15) Validation (초기부터 도입)

### 15.1 Validation이란?
API 입력값(RequestParam/PathVariable/RequestBody 등)을 검증하는 기능이다.

### 15.2 도입 이유
- 컨트롤러에 if-check 중복 방지
- 검증 실패 시 400 응답 처리 통일
- 누락/버그 감소

---

## 16) Flyway (초기부터 도입)

### 16.1 Flyway 설정 예시 (application.yml)
```yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

### 16.2 Flyway 파일 규칙
경로:
- src/main/resources/db/migration

예시:
- V1__init.sql
- V2__add_daily_price.sql
- V3__add_disclosure.sql

주의:
- 이미 적용된 파일은 수정하지 말고 새 버전 파일을 추가한다.

---

## 17) JPA ddl-auto 권장 설정

Flyway로 스키마를 관리할 때는 JPA가 임의로 변경하지 않게 한다.

```yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

---

## 18) Actuator (나중에 도입)

### 18.1 Actuator란?
운영/모니터링(health/metrics/info)을 위한 엔드포인트 제공 모듈이다.

### 18.2 도입 방침
- MVP 초반에는 미도입
- 배포/운영 필요 시 의존성 추가

(나중에) 최소 설정 예시:
```yml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics
```

---

## 19) 추가로 넣으면 좋은 것들(빠지면 나중에 고생함)

### A) 마이그레이션 도구
- Flyway 추천 (초기부터 넣는 게 편함)

### B) 운영/디버깅
- Spring Actuator (헬스체크)
- requestId 기반 로깅(MDC)

### C) Upsert 전략
일봉/공시는 “중복 가능” 데이터라 upsert가 필요함.
- JPA만으로 처리 어려우면 native query로 upsert 사용

### D) 시간/타임존 고정
- DB/서버/스케줄 기준을 KST(+09:00)로 고정하거나
- 내부는 UTC로 저장하고 표시만 KST로 변환(권장)

### E) 데이터 품질 체크(간단)
- 일봉 데이터 누락일 확인
- 공시 중복/누락 확인
- 비정상 급등락(0원, 거래량 0 등) 필터링

---

## 20) 추천 기술 스택(현실적인 MVP)

- Spring Boot 3.x
- Java 17+
- PostgreSQL
- Spring Data JPA
- WebClient
- Validation
- Flyway
- (선택) Redis: 캐시/락/배치 중복 실행 방지
- (나중에) Actuator: 모니터링

---

## 21) 최종 한 줄 요약

**Spring 모놀리식으로 시작하되, API / Ingest / Summary(정제)로 역할을 분리하고, SummaryService 하나를 중심으로 MVP를 완성한다.**  
**Flyway + Validation은 처음부터 도입하고, Actuator는 나중에 붙인다.**

