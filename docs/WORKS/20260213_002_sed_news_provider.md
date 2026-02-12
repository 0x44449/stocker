# 서울경제 뉴스 프로바이더 추가

## 목표

뉴스 소스에 서울경제(sed) 신문을 추가한다.

## 배경

현재 매일경제(mk), 한국경제(hk), 이투데이(etoday) 3개 소스에서 뉴스를 수집 중이다.
서울경제를 추가하여 뉴스 커버리지를 넓힌다.
기존 `NewsProvider` 인터페이스 구현체를 하나 만들면 `ProviderRegistry`가 자동 등록한다.

## 할 것

### 1. `provider/sed/` 패키지 생성 (3파일)

경로: `apps/api/src/main/java/com/hanzi/stocker/ingest/news/provider/sed/`

#### SedProvider.java
- 기존 Provider와 동일한 구조
- `id()`: `"sed"`
- `baseUrl()`: `"https://www.sedaily.com"`
- `press()`: `"서울경제"`
- `sitemapHints()`: `["https://www.sedaily.com/sitemap/latestnews"]`

#### SedUrlPolicy.java
- 기사 URL 패턴: `https://www.sedaily.com/article/{숫자}`
- 판별 조건: `/article/` 포함 + 마지막 경로가 숫자

#### SedArticleParser.java
- 제목: `meta[property=og:title]` → fallback `h1.title`
- 발행일: `meta[property=article:published_time]` (OffsetDateTime 형식: `2026-02-12T10:40:31+09:00`)
- 본문: `div#article-body[itemprop=articleBody]`
- 본문에서 제거할 요소:
  - `script, style, iframe`
  - `.article-photo-wrap` (이미지 영역)

## 안 할 것

- NewsCrawlEngine 수정 없음
- NewsProvider 인터페이스 변경 없음
- 기존 프로바이더(mk, hk, etoday) 수정 없음
- 스케줄러 변경 없음
- 사이트맵 파싱 로직 변경 없음 (Google News Sitemap이지만 `<url><loc>` 구조라 기존 엔진 호환)

## 참고

- 기존 프로바이더 패턴 참고: `provider/etoday/` (가장 최근 추가)
- 사이트맵 URL: `https://www.sedaily.com/sitemap/latestnews`
- 기사 샘플: `https://www.sedaily.com/article/20008365`
