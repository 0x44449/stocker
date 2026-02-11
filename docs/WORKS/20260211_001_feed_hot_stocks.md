# 피드 API — 핫 종목 카드

## 배경

모바일 피드 화면용 API를 `api/feed/` 패키지에 만든다.
첫 번째 카드는 "오늘의 핫 종목" — 오늘 뉴스에서 가장 많이 언급된 종목 상위 3개.

## 목표

`api/feed/` 패키지 생성 및 핫 종목 카드 API 구현.

## 관련 테이블

```
news_extraction   - extraction_id, news_id, keywords (JSONB), llm_model, prompt_version, created_at
stock_master      - stock_code, name_kr, name_kr_short
```

## 할 것

### 1. `api/feed/` 패키지 생성

### 2. 핫 종목 API

- `GET /api/feed/hot-stocks`
- 파라미터 없음 (오늘 날짜, 상위 3개, threshold 모두 하드코딩)

로직:
1. news_extraction에서 created_at이 오늘인 것 + llm_model/prompt_version 필터 (하드코딩) + keywords가 빈 배열이 아닌 것만 조회
2. stock_master의 name_kr, name_kr_short Set 구성
3. keywords 풀어서 Set에 있는 것만 카운팅
4. 상위 3개만 반환

응답:
```json
{
  "stocks": [
    { "rank": 1, "companyName": "SK하이닉스", "count": 52 },
    { "rank": 2, "companyName": "삼성전자", "count": 48 },
    { "rank": 3, "companyName": "LG화학", "count": 31 }
  ]
}
```

- HeadlineService를 호출하지 않고 독립적으로 구현
- articles 목록 포함하지 않음

## 안 할 것

- HeadlineService 재사용/의존
- 기간 파라미터 (이번 주 등)
- 상위 N개 파라미터
- 기사 목록 포함
