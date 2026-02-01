# Admin Web - 기업명-종목 매핑 관리 페이지

## 배경

- company_name_mapping 테이블에 자동 매칭 결과가 쌓이고 있음
- 정확 일치 외 미매칭 건은 수동으로 처리해야 함
- 학습 데이터 수집을 위해 수동 매핑 UI 필요

## 목표

기업명-종목 수동 매핑을 위한 관리 페이지 구현

## 기술 스택

- Next.js
- Tailwind CSS
- shadcn/ui
- 위치: apps/admin-web

## 할 것 (Phase 1: 프론트엔드)

### 1. 프로젝트 초기 설정

```bash
cd apps
npx create-next-app@latest admin-web --typescript --tailwind --eslint --app --src-dir
npx shadcn@latest init
```

### 2. 레이아웃

사이드바 네비게이션 + 콘텐츠 영역 구조.

**사이드바 메뉴:**
- 대시보드 (향후)
- 뉴스 매핑

### 3. 뉴스 매핑 목록 화면 (/mappings)

뉴스 단위로 목록 표시. 뉴스 1개에 기업명 N개, 종목 N개 매핑 가능.

```
┌──────────────────────────────────────────────────────────────┐
│ [필터: 전체 | 미매칭있음 | 완료]  [검색: ____]               │
├──────────────────────────────────────────────────────────────┤
│ 뉴스 제목                    │ 추출 기업명       │ 매칭상태  │
├──────────────────────────────────────────────────────────────┤
│ 현대차그룹 전기차 투자...    │ 현대차그룹 외 2   │ 1/3 매칭  │
│ 삼성전자 실적 발표...        │ 삼성전자          │ 1/1 완료  │
│ 애플·구글 AI 경쟁...         │ 애플, 구글        │ 0/2 미매칭│
├──────────────────────────────────────────────────────────────┤
│                    < 1 2 3 4 5 >                             │
└──────────────────────────────────────────────────────────────┘
```

**기능:**
- 필터: 전체 / 미매칭있음 / 완료
- 검색: 뉴스 제목, 기업명 검색
- 페이지네이션
- 행 클릭 → 매핑 화면 이동

### 4. 매핑 상세 화면 (/mappings/{newsId})

뉴스 1개 선택 시, 해당 뉴스의 모든 기업명을 카드 형태로 표시.
각 기업명별로 종목 다중 선택 가능. 종목별로 매핑 사유 입력.

```
┌─────────────────────────────────────────────────────────────┐
│ 뉴스: "현대차그룹 전기차 투자 확대..."          [뉴스 보기] │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ ┌─ 기업명: 현대차그룹 ─────────────────────────────────┐   │
│ │ 종목 검색: [________] 🔍                             │   │
│ │                                                      │   │
│ │ ☑ 현대자동차 (005380)                                │   │
│ │   └ 사유: [그룹 대표 상장사______________]           │   │
│ │                                                      │   │
│ │ ☑ 현대모비스 (012330)                                │   │
│ │   └ 사유: [주요 부품 계열사______________]           │   │
│ │                                                      │   │
│ │ ☐ 현대글로비스 (086280)                              │   │
│ │                                                      │   │
│ │ ☐ 해당 없음                                          │   │
│ │   └ 사유: [____________________________]             │   │
│ └──────────────────────────────────────────────────────┘   │
│                                                             │
│ ┌─ 기업명: 애플 ───────────────────────────────────────┐   │
│ │ ☑ 해당 없음                                          │   │
│ │   └ 사유: [미국 기업, 국내 상장 없음___]             │   │
│ └──────────────────────────────────────────────────────┘   │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                    [취소]  [저장]                            │
└─────────────────────────────────────────────────────────────┘
```

**기능:**
- 기업명별 카드로 표시
- 종목 검색 (name_kr, name_kr_short)
- 다중 선택 (체크박스)
- 종목별 매핑 사유 입력 (feedback)
- "해당 없음" 옵션 (비상장, 외국기업 등) + 사유
- 자동 매칭도 수정 가능 (해제, 변경, 추가)

**데이터 구조:**
같은 extracted_name으로 여러 행 생성 (종목별 1행)
```
news_id=1, extracted_name="현대차그룹", matched_stock_code="005380", feedback="그룹 대표 상장사"
news_id=1, extracted_name="현대차그룹", matched_stock_code="012330", feedback="주요 부품 계열사"
```

**중복 허용:**
- 학습용 원천 데이터로 취급
- 같은 news_id에 같은 stock_code가 여러 번 매핑될 수 있음
- 예: "현대차그룹"→현대자동차, "현대자동차"→현대자동차 둘 다 저장 가능
- 실제 사용 시 가공하여 중복 제거

**저장 시:**
- 기존 매핑 삭제 후 새로 생성 (또는 diff 계산하여 처리)
- match_type = 'manual' (수정된 경우)
- verified = true
- feedback 저장

### 5. API 엔드포인트 명세 (참고용)

프론트엔드 개발 시 참고할 API 명세. 실제 구현은 Phase 2에서.

```
# 뉴스 목록 (매핑 요약 포함)
GET  /api/admin/news-mappings?filter=&page=&size=&search=

# 뉴스별 매핑 상세
GET  /api/admin/news-mappings/{newsId}

# 매핑 수정 (upsert)
PUT  /api/admin/mappings
  body: {
    newsId: 1,
    mappings: [
      { id: 123, stockCode: "005380", feedback: "수정 사유" },  // 기존 수정
      { id: 124, stockCode: null, feedback: "잘못된 매칭" },     // 해제 (해당없음)
      { id: null, stockCode: "373220", feedback: "배터리 관련" }  // 신규 추가
    ]
  }

# 종목 검색
GET  /api/admin/stocks?search=
```

**API 규칙:**
- id 있으면 UPDATE, 없으면 INSERT
- stockCode=null 이면 "해당없음" 처리
- 신규 추가 시 extracted_name=null로 저장 (LLM이 못뽑았음을 의미)
- match_type = 'manual', verified = true 로 설정

**학습 데이터 관점:**
| extracted_name | stockCode | 의미 |
|---|---|---|
| 있음 | 있음 | LLM이 뽑음 + 매핑됨 |
| 있음 | null | LLM이 뽑았는데 잘못됨/해당없음 |
| null | 있음 | LLM이 못뽑음 + 사람이 매핑 |
| null | null | (의미없음, 생성 안함) |

## 할 것 (Phase 2: 서버 API)

### 1. DB 마이그레이션

extracted_name nullable로 변경:
```sql
-- V10__alter_company_name_mapping_extracted_name_nullable.sql
ALTER TABLE company_name_mapping ALTER COLUMN extracted_name DROP NOT NULL;
```

### 2. API 엔드포인트 구현

- GET /api/admin/news-mappings
- GET /api/admin/news-mappings/{newsId}
- PUT /api/admin/mappings
- GET /api/admin/stocks

## 안 할 것

- 인증/권한 (나중에)
- 같은 기업명 일괄 매핑 (나중에)
- 연속 작업 편의 기능 (나중에)
- 통계/대시보드

## 데이터 플로우

```
자동 매칭 (auto_exact, verified=false)
    ↓ 사람이 확인
맞으면 → verified=true, feedback 작성
틀리면 → 수정 후 match_type=manual, verified=true, feedback 작성

미매칭 (none)
    ↓ 사람이 매핑
match_type=manual, verified=true, feedback 작성
```

## 완료 조건

### Phase 1
- [ ] Next.js + Tailwind + shadcn/ui 프로젝트 생성
- [ ] 사이드바 네비게이션 레이아웃
- [ ] 뉴스 매핑 목록 화면 (필터, 검색, 페이지네이션)
- [ ] 매핑 상세 화면 (기업명별 카드, 다중 선택, 사유 입력)
- [ ] Docker 설정

### Phase 2
- [ ] DB 마이그레이션 (extracted_name nullable)
- [ ] Spring Boot API 엔드포인트 구현
