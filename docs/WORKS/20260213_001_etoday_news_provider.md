# 이투데이 뉴스 프로바이더 추가

## 목표

뉴스 소스에 이투데이(etoday) 신문을 추가한다.

## 배경

현재 매일경제(mk), 한국경제(hk) 2개 소스에서 뉴스를 수집 중이다.
이투데이를 추가하여 뉴스 커버리지를 넓힌다.
기존 `NewsProvider` 인터페이스 구현체를 하나 만들면 `ProviderRegistry`가 자동 등록한다.

## 할 것

### 1. `provider/etoday/` 패키지 생성 (3파일)

경로: `apps/api/src/main/java/com/hanzi/stocker/ingest/news/provider/etoday/`

#### EtodayProvider.java
- MK/HK Provider와 동일한 구조
- `id()`: `"etoday"`
- `baseUrl()`: `"https://www.etoday.co.kr"`
- `press()`: `"이투데이"`
- `sitemapHints()`: `["https://www.etoday.co.kr/rss/news_sitemap"]`

#### EtodayUrlPolicy.java
- 기사 URL 패턴: `https://www.etoday.co.kr/news/view/{숫자}`
- 판별 조건: `/news/view/` 포함 + 마지막 경로가 숫자

#### EtodayArticleParser.java
- 제목: `meta[property=og:title]` → fallback `h1.main_title`
- 발행일: `meta[property=article:published_time]` (OffsetDateTime 형식: `2026-02-12T17:05:00+09:00`)
- 본문: `div.articleView[itemprop=articleBody]`
- 본문에서 제거할 요소:
  - `script, style, iframe`
  - `.img_box_C` (이미지 박스)
  - `[id^=div-gpt-ad]` (구글 광고)
  - `.relation_newslist` (관련 뉴스)
  - `.kwd_tags` (키워드 태그)
  - `.card-container` (기업 네임카드)
  - `.reporter_topNews` (기자 주요 뉴스)
  - `.recommend_btn` (좋아요/화나요 등)
  - `.ico_share` (공유 버튼)

## 안 할 것

- NewsCrawlEngine 수정 없음
- NewsProvider 인터페이스 변경 없음
- 기존 프로바이더(mk, hk) 수정 없음
- 스케줄러 변경 없음
- 사이트맵 파싱 로직 변경 없음 (Google News Sitemap이지만 `<url><loc>` 구조라 기존 엔진 호환)

## 참고

- 기존 프로바이더 패턴 참고: `provider/mk/`, `provider/hk/`
- 사이트맵 URL: `https://www.etoday.co.kr/rss/news_sitemap`
- 기사 샘플: `https://www.etoday.co.kr/news/view/2556417`
