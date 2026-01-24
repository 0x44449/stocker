# News Crawler Detailed Structure Design (v0)

> **Base package:** `com.hanzi.stocker`

이 문서는 **코드 작성 AI가 그대로 따라 구현해야 하는 상세 구조 설계 문서**이다.
클래스명, 책임, DTO/Entity 경계, 패키지 구조는 **강제 규칙**이며 임의 변경을 허용하지 않는다.

---

## 0. 설계 목표

* 언론사 뉴스 크롤링을 **독립된 ingest 파이프라인**으로 구현한다
* raw 데이터(`news_raw`)는 **도메인 로직과 완전히 분리**한다
* DTO / Entity / Service 책임을 명확히 구분한다
* 향후 언론사 추가 시 **구조 변경 없이 확장** 가능해야 한다

---

## 1. 패키지 구조 (강제)

```text
com.hanzi.stocker.ingest.news
├─ crawler
│  ├─ NewsCrawler
│  ├─ NewsCrawlContext
│  └─ RobotsTxtPolicy
├─ sitemap
│  ├─ SitemapClient
│  ├─ SitemapParser
│  └─ SitemapEntry
├─ article
│  ├─ ArticleClient
│  ├─ ArticleParser
│  └─ ParsedArticle
├─ raw
│  ├─ NewsRawEntity
│  ├─ NewsRawRepository
│  ├─ NewsRawService
│  └─ NewsRawMapper
└─ config
   └─ NewsCrawlerConfig
```

> ❗ `ingest.news` 외부에서 crawler 내부 클래스를 직접 호출하면 안 된다.

---

## 2. Crawler 레이어

### 2.1 NewsCrawler (진입점)

**역할**

* 크롤링 전체 흐름 오케스트레이션
* robots → sitemap → article → 저장 순서 제어

```text
class: NewsCrawler
```

**책임**

* 실행 순서 제어
* 개별 단계 실패 시 다음 단계로 진행
* sleep / rate-limit 적용

❌ HTML 파싱
❌ DB 직접 접근

---

### 2.2 NewsCrawlContext

**역할**

* 크롤링 실행 시 필요한 공통 상태 보관

```text
fields:
- source
- baseUrl
- userAgent
- maxArticles
- crawlStartedAt
```

---

### 2.3 RobotsTxtPolicy

**역할**

* robots.txt 파싱 결과를 표현하는 정책 객체

```text
fields:
- disallowedPaths: Set<String>
```

**메서드 개념**

* `isAllowed(String path): boolean`

---

## 3. Sitemap 레이어

### 3.1 SitemapClient

**역할**

* sitemap XML 다운로드

```text
input: sitemapUrl
output: raw XML string
```

---

### 3.2 SitemapParser

**역할**

* sitemap XML → 엔트리 목록 변환

```text
input: xml
output: List<SitemapEntry>
```

---

### 3.3 SitemapEntry (DTO)

```text
fields:
- loc (URL)
- lastModified (optional)
```

> ❗ Entity 아님 (DB와 무관)

---

## 4. Article 레이어

### 4.1 ArticleClient

**역할**

* 기사 HTML 요청

```text
input: articleUrl
output: html string
```

---

### 4.2 ArticleParser

**역할**

* HTML → ParsedArticle 변환

```text
input: html
output: ParsedArticle
```

---

### 4.3 ParsedArticle (DTO)

```text
fields:
- title
- rawText       (≤ 2000 chars)
- publishedAt
- press
```

❌ 요약
❌ 종목 코드

---

## 5. Raw 저장 레이어

### 5.1 NewsRawEntity (JPA Entity)

```text
table: news_raw

fields:
- id (PK)
- source
- press
- title
- rawText
- url
- publishedAt
- collectedAt
- expiresAt
```

> ❗ `rawText` 길이 제한은 **비즈니스 로직에서 보장**한다.

---

### 5.2 NewsRawRepository

* Spring Data JPA Repository
* 단순 save 전용

❌ 복잡한 조회
❌ 분석 쿼리

---

### 5.3 NewsRawService

**역할**

* ParsedArticle → Entity 저장 책임

```text
methods:
- save(ParsedArticle article, NewsCrawlContext ctx)
```

**책임**

* collectedAt / expiresAt 계산
* rawText 길이 검증
* 빈 텍스트 필터링

---

### 5.4 NewsRawMapper

**역할**

* DTO → Entity 변환 전용

❌ DB 접근
❌ 시간 계산

---

## 6. Config 레이어

### 6.1 NewsCrawlerConfig

```text
fields:
- userAgent
- maxArticles
- crawlDelaySeconds
- rawTextMaxLength (2000)
- rawRetentionDays (30)
```

> ❗ 상수 하드코딩 금지

---

## 7. DTO / Entity 경계 규칙 (강제)

| 구분                   | 규칙                   |
| -------------------- | -------------------- |
| DTO                  | HTML / XML 파싱 결과만 담음 |
| Entity               | DB 저장용               |
| Entity → DTO         | 금지                   |
| DTO → Entity         | Mapper 통해서만          |
| Crawler → Repository | 금지                   |

---

## 8. 설계 Red Lines (절대 금지)

* NewsCrawler에서 DB 접근
* ArticleParser에서 Entity 생성
* raw 데이터 API 응답 노출
* DTO에 JPA 어노테이션 사용
* sitemap/robots 로직 생략

---

## 9. 확장 시 가이드

* 언론사 추가:

  * `NewsCrawlerConfig` 추가
  * sitemap URL만 교체
* 요약 추가:

  * `ingest.news.summary` 패키지 신규 생성
  * raw 수정 금지

---

## 10. 설계 요약 한 줄

> **Crawler는 조율자,
> Parser는 변환기,
> Service는 저장 책임자다.**

이 역할이 섞이면 설계 위반이다.
