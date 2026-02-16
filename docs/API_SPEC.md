# Stocker API 명세

이 문서는 `UI_SPEC.md`의 화면별 정보 요구사항을 기반으로 클라이언트 API를 정의한다.
"이 화면에 이 데이터가 필요하니까 이 API가 있어야 한다"가 기준이다.

---

## API 전체 목록

| # | 메서드 | 엔드포인트 | 서빙 화면 | 설명 |
|---|--------|-----------|-----------|------|
| 1 | GET | `/api/feed` | 메인 피드 | 관심종목+화제성 클러스터 통합 피드 |
| 2 | GET | `/api/watchlist/dashboard` | 관심종목 탭 | 종목별 현재가+최근 클러스터 일괄 조회 |
| 3 | GET | `/api/stocks/{code}/timeline` | 종목 상세 | 종목의 클러스터 타임라인 |
| 4 | GET | `/api/clusters/{clusterId}` | 기사 목록 | 클러스터 상세 + 소속 기사 목록 |
| 5 | GET | `/api/clusters/{clusterId}/articles` | 기사 목록 (페이징) | 기사가 많을 경우 별도 페이징 |
| 6 | GET | `/api/watchlist` | 관심종목 설정 | 관심종목 목록 조회 |
| 7 | POST | `/api/watchlist/{code}` | 관심종목 설정 | 관심종목 추가 |
| 8 | DELETE | `/api/watchlist/{code}` | 관심종목 설정 | 관심종목 삭제 |
| 9 | GET | `/api/stocks/search?query=` | 관심종목 설정 | 종목 검색 |


---

## 상세 명세

### 1. `GET /api/feed`

**서빙 화면**: 메인 피드

**설명**: 관심종목 클러스터 + 화제성 클러스터를 합쳐서 시간순 정렬한 피드를 반환한다.

**파라미터**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `cursor` | String | N | 페이지네이션 커서 |
| `size` | int | N | 한 페이지 건수 (기본 20) |

**응답**:
```json
{
  "items": [
    {
      "type": "NEWS_CLUSTER",
      "clusterId": 1,
      "category": "실적",
      "headline": "삼성전자 2분기 실적, 시장 기대치 상회",
      "summary": "영업이익 10.4조원으로 전년 대비 15배 증가.",
      "articleCount": 12,
      "firstArticleTime": "2026-02-15T16:02:00",
      "marketSession": "AFTER",
      "stocks": [
        {
          "stockCode": "005930",
          "stockName": "삼성전자",
          "role": "주체",
          "changeRate": 2.3
        },
        {
          "stockCode": "000660",
          "stockName": "SK하이닉스",
          "role": "동종업계",
          "changeRate": 1.1
        }
      ]
    }
  ],
  "nextCursor": "..."
}
```

**피드 구성 로직** (서버에서 처리):
1. 관심종목 코드 목록 조회
2. 해당 종목이 포함된 클러스터 전부 포함
3. 기사 수 상위 N개 클러스터 포함 (화제성)
4. 멘션 급증 종목 클러스터 포함
5. 중복 제거, firstArticleTime 역순 정렬

> MVP 이후 추가: 연관종목 클러스터 (동일 섹터, 동시 언급)

---

### 2. `GET /api/watchlist/dashboard`

**서빙 화면**: 관심종목 탭

**설명**: 관심종목별 현재가 + 최근 주요 클러스터를 한번에 반환한다.

**응답**:
```json
{
  "stocks": [
    {
      "stockCode": "005930",
      "stockName": "삼성전자",
      "price": 67200,
      "changeRate": 2.3,
      "changeAmount": 1500,
      "todayNewsCount": 12,
      "recentClusters": [
        {
          "clusterId": 1,
          "category": "실적",
          "headline": "삼성전자 2분기 실적, 시장 기대치 상회",
          "firstArticleTime": "2026-02-15T16:02:00",
          "articleCount": 12,
          "dayChangeRate": 2.3
        }
      ],
      "totalClusterCount": 6
    }
  ]
}
```

**recentClusters**: 최근 클러스터 상위 3건.

**totalClusterCount**: 3건 이상이면 클라이언트에서 "전체보기" 표시.

---

### 3. `GET /api/stocks/{code}/timeline`

**서빙 화면**: 종목 상세

**설명**: 특정 종목의 주가 정보 + 클러스터를 시간 역순으로 반환한다.

**파라미터**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `cursor` | String | N | 페이지네이션 커서 |
| `size` | int | N | 한 페이지 건수 (기본 20) |

**응답**:
```json
{
  "stock": {
    "stockCode": "005930",
    "stockName": "삼성전자",
    "price": 67200,
    "changeRate": 2.3,
    "changeAmount": 1500
  },
  "timeline": [
    {
      "clusterId": 1,
      "category": "실적",
      "headline": "삼성전자 2분기 실적, 시장 기대치 상회",
      "firstArticleTime": "2026-02-15T16:02:00",
      "articleCount": 12,
      "dayChangeRate": 2.3
    }
  ],
  "nextCursor": "..."
}
```

**dayChangeRate**: 첫 기사 발행일의 해당 종목 등락률.

---

### 4. `GET /api/clusters/{clusterId}`

**서빙 화면**: 기사 목록

**설명**: 특정 클러스터의 상세 정보와 소속 기사 목록을 반환한다.

**응답**:
```json
{
  "clusterId": 1,
  "category": "실적",
  "headline": "삼성전자 2분기 실적, 시장 기대치 상회",
  "summary": "영업이익 10.4조원으로 전년 대비 15배 증가.",
  "firstArticleTime": "2026-02-15T16:02:00",
  "marketSession": "AFTER",
  "articleCount": 12,
  "stocks": [
    {
      "stockCode": "005930",
      "stockName": "삼성전자",
      "role": "주체",
      "changeRate": 2.3
    }
  ],
  "articles": [
    {
      "newsId": 12345,
      "title": "삼성전자, 2분기 영업이익 10.4조원 기록",
      "press": "매일경제",
      "publishedAt": "2026-02-15T16:02:00",
      "url": "https://..."
    }
  ]
}
```

---

### 5. `GET /api/clusters/{clusterId}/articles`

**서빙 화면**: 기사 목록 (기사가 많을 경우)

**설명**: 클러스터 소속 기사 목록만 페이징으로 반환. 기사가 적으면 4번 API의 articles로 충분하지만, 기사가 많을 경우를 대비.

**파라미터**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `cursor` | String | N | 페이지네이션 커서 |
| `size` | int | N | 한 페이지 건수 (기본 20) |

**응답**:
```json
{
  "articles": [
    {
      "newsId": 12345,
      "title": "삼성전자, 2분기 영업이익 10.4조원 기록",
      "press": "매일경제",
      "publishedAt": "2026-02-15T16:02:00",
      "url": "https://..."
    }
  ],
  "nextCursor": "..."
}
```

> MVP에서는 4번으로 충분할 수 있음. 기사 수가 많아지면 분리.

---

### 6. `GET /api/watchlist`

**서빙 화면**: 관심종목 설정

**설명**: 등록된 관심종목 목록 반환.

**응답**:
```json
{
  "stocks": [
    {
      "stockCode": "005930",
      "stockName": "삼성전자"
    }
  ]
}
```

---

### 7. `POST /api/watchlist/{code}`

**서빙 화면**: 관심종목 설정

**설명**: 관심종목 추가.

**응답**: 200 OK / 404 종목 없음 / 409 이미 등록됨

---

### 8. `DELETE /api/watchlist/{code}`

**서빙 화면**: 관심종목 설정

**설명**: 관심종목 삭제.

**응답**: 200 OK / 404 등록되지 않은 종목

---

### 9. `GET /api/stocks/search?query=`

**서빙 화면**: 관심종목 설정 (검색)

**설명**: 종목명 또는 종목코드로 검색.

**파라미터**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `query` | String | Y | 검색어 (2자 이상) |

**응답**:
```json
{
  "stocks": [
    {
      "stockCode": "005930",
      "stockName": "삼성전자",
      "stockNameShort": "삼성전자",
      "market": "KOSPI"
    }
  ]
}
```

---

---

## 화면 → API 매핑

| 화면 | 사용 API |
|------|----------|
| 메인 피드 | `#1 feed` |
| 관심종목 탭 | `#2 watchlist/dashboard` |
| 종목 상세 | `#3 stocks/{code}/timeline` |
| 기사 목록 | `#4 clusters/{clusterId}` (+ `#5` 필요 시) |
| 관심종목 설정 | `#6 watchlist` + `#7 추가` + `#8 삭제` + `#9 검색` |

---

## 화면 전환 시 API 호출 흐름

```
메인 피드 (#1 feed)
  │
  ├─ 카드 탭 → #4 clusters/{clusterId}  ← clusterId는 #1 응답에 포함
  │
  └─ 종목 태그 탭 → #3 stocks/{code}/timeline  ← stockCode는 #1 응답의 stocks에 포함

관심종목 탭 (#2 watchlist/dashboard)
  │
  ├─ 클러스터 항목 탭 → #4 clusters/{clusterId}  ← clusterId는 #2 응답의 recentClusters에 포함
  │
  ├─ 종목 헤더 탭 → #3 stocks/{code}/timeline  ← stockCode는 #2 응답에 포함
  │
  └─ 전체보기 → #3 stocks/{code}/timeline

종목 상세 (#3 stocks/{code}/timeline)
  │
  └─ 타임라인 항목 탭 → #4 clusters/{clusterId}  ← clusterId는 #3 응답의 timeline에 포함

기사 목록 (#4 clusters/{clusterId})
  │
  └─ 기사 탭 → 웹뷰 (url)  ← url은 #4 응답의 articles에 포함
```

> 모든 화면 전환에 필요한 ID(clusterId, stockCode, url)는 이전 API 응답에 포함되어 있다.
> 클라이언트가 별도로 ID를 조합하거나 추측할 필요 없음.

---

## 공통 규칙

### 페이지네이션
- 커서 기반. offset 사용하지 않음.
- `cursor`는 opaque string (클라이언트가 내부 구조를 알 필요 없음)
- `nextCursor`가 null이면 마지막 페이지

### 시간 형식
- ISO 8601: `2026-02-15T16:02:00`
- 타임존: KST (서버에서 KST 기준으로 반환)

### 장 상태 (marketSession)
- `BEFORE`: 장전 (~09:00)
- `OPEN`: 장중 (09:00~15:30)
- `AFTER`: 장후 (15:30~)

### 에러 응답
```json
{
  "error": "NOT_FOUND",
  "message": "종목을 찾을 수 없습니다"
}
```

### 인증
- MVP에서는 없음. 디바이스 ID 기반 또는 하드코딩.
- 관심종목은 디바이스별로 로컬 저장 + 서버 동기화 (이후)

---

*이 문서는 UI_SPEC.md와 함께 유지 관리한다. 화면 변경 시 API도 함께 업데이트.*
