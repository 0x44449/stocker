## 1) 큰 그림: 엔진 + 플러그인(Provider) 구조

### 핵심 아이디어

* **공통 파이프라인(엔진)**: robots → sitemap → fetch → parse → 저장
* **언론사별 차이(플러그인)**: sitemap 위치, 기사 URL 필터, 본문 selector, 발행시각 파싱

즉, “크롤러 1개로 다 처리”가 아니라
**크롤링 엔진 1개 + Provider N개**로 간다.

---

## 2) 패키지/모듈 구조 (권장)

```text
com.hanzi.stocker.ingest.news
├─ engine
│  ├─ NewsCrawlEngine
│  ├─ CrawlPlan
│  ├─ CrawlRateLimiter
│  ├─ RobotsService
│  ├─ SitemapService
│  └─ HttpFetcher
├─ provider
│  ├─ NewsProvider                (interface)
│  ├─ ProviderRegistry
│  ├─ mk
│  │  ├─ MkProvider
│  │  ├─ MkArticleParser
│  │  └─ MkUrlPolicy
│  └─ ... (언론사별 패키지)
├─ model
│  ├─ SitemapEntry
│  ├─ ParsedArticle
│  └─ FetchResult
└─ raw
   ├─ NewsRawEntity
   ├─ NewsRawRepository
   ├─ NewsRawService
   └─ NewsRawMapper
```

> “engine은 절대 언론사 HTML 규칙을 모르고, provider가 다 갖고 있다”가 핵심.

---

## 3) Provider 인터페이스 설계 (가장 중요)

언론사별 차이를 **여기**로 “완전히 격리”시키는 게 승부처야.

### 최소 인터페이스 (v0)

* `id()` : `"mk"` 같은 소스 키
* `baseUrl()` : robots/sitemap 요청 기준
* `sitemapHints()` : robots.txt에 sitemap이 없을 때도 대비한 힌트(선택)
* `isArticleUrl(url)` : sitemap에서 가져온 loc 중 기사만 추림
* `parseArticle(html, url)` : 본문/제목/발행시간 뽑기
* `policy()` : 2000자 제한, 30일 보관 같은 공통 규칙은 엔진에서 하되, provider 추가 정책이 있으면 여기에

### 추천 형태

* `NewsProvider`는 “정책 + 파싱”까지만 담당
* “HTTP 요청/저장/중복”은 엔진과 raw 서비스가 담당

---

## 4) 엔진(NewsCrawlEngine) 실행 흐름

### Step A. Provider 선택

* `ProviderRegistry`가 Spring Bean으로 등록된 Provider들을 들고 있음
* 설정으로 enable/disable 가능 (`stocker.ingest.news.providers.mk.enabled=true`)

### Step B. robots.txt 읽기

* `RobotsService.fetchPolicy(baseUrl, userAgentName)`
* `User-agent: *` 기준 `Disallow`만 파싱
* Provider가 “이 도메인에서 robots를 강제할지”는 엔진 전역 정책으로 고정(강제 권장)

### Step C. sitemap URL 결정

1. robots.txt에 `Sitemap:`가 있으면 그걸 **1순위**
2. 없으면 provider의 `sitemapHints()` 사용 (있을 때만)
3. 둘 다 없으면 **그 provider는 skip** (설계상 안전)

### Step D. sitemap parsing + 기사 URL 추출

* `SitemapService.fetchAndParse(sitemapUrl)` → `List<SitemapEntry>`
* `provider.isArticleUrl(entry.loc)`로 필터
* v0: 최근 N개만 처리 (예: 5~10)

### Step E. 기사 fetch + parse

* `HttpFetcher.get(url)` (timeout / redirect / gzip 처리)
* 403/429면 즉시 중단(해당 provider만)하고 다음 provider로
* `provider.parseArticle(html, url)` → `ParsedArticle`

### Step F. raw 저장

* `NewsRawService.save(providerId, parsedArticle, url, now)`
* 여기서:

  * raw_text 2000자 자르기
  * collectedAt = now
  * expiresAt = now + 30d
  * 빈 본문 skip
  * (가능하면) 중복 key로 upsert (아래 참고)

### Step G. rate limit

* 도메인 단위로 `sleep 1~3초`
* 병렬 처리 금지 (초기엔 절대)

---

## 5) 중복 처리(강력 권장)

언론사 사이트는 종종:

* 같은 기사 URL이 sitemap에 반복
* 파라미터만 다른 URL
* 재발행/수정으로 lastmod만 바뀜

### v0에서 제일 현실적인 Unique Key

* `unique(source, url)`

  * URL이 깨끗하면 이게 베스트
* URL이 자주 바뀌는 사이트면:

  * `unique(source, canonical_url_or_external_id)` (가능하면)
* external_id를 못 얻으면:

  * `unique(source, title_hash + published_at)` 같은 보조키

**중복 정책은 raw 테이블에서 바로 막는 게 안정적**이야.

---

## 6) Provider 구현 방식 (언론사별 “플러그인” 만들기)

언론사별로 **3개의 파일** 패턴을 추천해:

1. `XxxProvider` : 엔진이 호출하는 Facade
2. `XxxArticleParser` : HTML 파싱 전담 (Jsoup 기반 추천)
3. `XxxUrlPolicy` : “기사 URL만” 골라내는 규칙

이렇게 나누면:

* HTML 파싱 바뀌어도 URL 규칙은 그대로
* sitemap 구조 바뀌어도 파서는 그대로

---

## 7) HTML 파싱 전략 (실전 팁)

크롤러는 “정답 selector”가 매번 변해. 그래서 v0 목표는 이거야:

* **최소한의 안정성**
* 실패해도 전체 중단 안 함
* 파싱 실패율을 로그로 관측

### 파싱 순서(권장)

1. 제목: `og:title` → `<title>` → h1 후보
2. 본문:

   * `<article>` 우선
   * 없으면 “본문 컨테이너” 후보 selector들 시도 (provider별)
   * 텍스트 정제: 광고/기자메일/저작권 문구 제거(최소한)
3. 발행시각:

   * `meta[property=article:published_time]` → time 태그 → JSON-LD → 화면 텍스트 fallback
4. press:

   * provider 고정 값으로 둬도 됨(v0)

---

## 8) 설정(Config) 설계

`application.yml`에 이런 수준으로만 둬도 충분해:

* 전역:

  * userAgent
  * delaySeconds
  * maxArticlesPerProvider
  * rawTextMaxLength(2000)
  * rawRetentionDays(30)
* provider별:

  * enabled
  * sitemapHints(optional)

Provider가 많아질수록 **“enabled toggle”이 운영 생명줄**이 된다.

---

## 9) 테스트 전략 (이게 진짜 중요)

크롤러는 “돌려보면 깨짐”이 반복되니까, 테스트는 이렇게 간다.

### Provider 단위 테스트가 핵심

* 실제 HTTP 호출 X
* “기사 HTML 스냅샷 파일”을 test resource로 저장
* `XxxArticleParser.parse(html)` 결과가 유지되는지 검증

이렇게 해두면:

* 사이트 HTML이 바뀌면 테스트가 깨지고
* “어디가 바뀌었는지”를 바로 알 수 있음

---

## 10) 구현 순서 추천 (가장 빠르게 성공하는 루트)

1. `NewsRawEntity/Repository/Service` 먼저
2. `NewsProvider` 인터페이스 확정
3. `MkProvider` 1개만 구현
4. `NewsCrawlEngine`으로 provider 1개만 돌리기
5. 저장이 되면:

   * 중복 unique key 추가
   * 403/429 대응
   * provider 2개로 확장

---

