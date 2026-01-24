# News Crawler Implementation Guidelines (v0)

## 0. 목적 (Purpose)

이 크롤러의 목적은 **언론사 뉴스 원문을 수집·보관하는 것**이 아니라,  
**후속 데이터 처리(요약/매핑)를 위한 원천(raw) 텍스트 데이터를 제한적으로 수집**하는 것이다.

- 뉴스 분석 ❌
- 종목 매핑 ❌
- 요약 생성 ❌
- AI 처리 ❌

**“저장까지 되면 성공”** 이다.

---

## 1. 크롤링 대상 및 범위

### 1.1 대상
- 언론사 1곳부터 시작
- 네이버 뉴스 ❌
- 언론사 공식 사이트 ⭕
- robots.txt 및 sitemap 제공 사이트만 대상

### 1.2 시작점
- robots.txt에 명시된 sitemap URL
- 예:
  - `/sitemap/latest-articles/`
  - `/sitemap/daily-articles/`

URL 추측, 내부 링크 무작위 탐색 금지.

---

## 2. robots.txt 준수 규칙 (필수)

1. 크롤러 시작 시 `/robots.txt`를 반드시 요청한다.
2. `User-agent: *` 기준의 `Disallow` 규칙을 파싱한다.
3. Disallow 경로에 포함된 URL은 **절대 요청하지 않는다**.
4. AI/학습/대량 수집용 봇을 사칭하지 않는다.

### 요청 속도 제한
- 동일 도메인 요청 간 최소 1~3초 sleep
- 병렬 요청 금지

robots.txt를 무시한 구현은 **설계 위반**이다.

---

## 3. User-Agent 정책

- GPTBot, ChatGPT, ClaudeBot 등 AI 봇 User-Agent 사용 금지
- 일반 브라우저 스타일 User-Agent 사용
- 크롤러 식별 문자열 포함 가능 (예: contact/email)

---

## 4. Sitemap 처리 규칙

- sitemap XML에서 `<loc>` 만 사용
- `<lastmod>` 가 있으면 published_at 추정에 사용 가능
- v0 기준:
  - 최근 N개 (예: 5~10개)만 처리
  - 전체 sitemap 순회 금지

---

## 5. 기사 페이지 처리 규칙

### 허용
- HTML 요청
- `<article>`, `<div>` 기반 본문 텍스트 추출
- HTML 태그 제거 후 순수 텍스트만 사용

### 금지
- 이미지 다운로드
- iframe/script 처리
- 댓글/광고 영역 파싱
- 원문 HTML 저장

---

## 6. Raw 텍스트 저장 정책 (강제)

- 저장 대상: 기사 본문 텍스트만
- 최대 길이: **2000자**
- 초과 시: 앞부분부터 자른다
- 빈 텍스트: 저장하지 않는다

### 법적/운영 가드레일
- 전문 HTML 저장 ❌
- 이미지 저장 ❌
- 외부 API/UI 노출 ❌

---

## 7. news_raw 테이블 저장 규칙

### 저장 필드 (v0)

- source            (언론사 식별자)
- press             (언론사명)
- title             (기사 제목)
- raw_text          (본문 텍스트, ≤ 2000자)
- url               (원문 링크)
- published_at      (기사 발행 시각)
- collected_at      (수집 시각)
- expires_at        (collected_at + 30일)

### 금지 필드
- 종목 코드(code)
- 요약 결과
- AI 분석 결과
- 중요도/감성 점수

---

## 8. 데이터 보관 정책

- raw 데이터는 **30일 보관 후 자동 삭제**
- raw 데이터는 재처리용 캐시 성격
- derived 데이터(요약/팩트)는 raw 삭제 이후에도 유지 가능

---

## 9. 실패 및 예외 처리

- 기사 1건 실패 → 전체 중단 ❌
- 로그 기록 후 다음 기사 진행
- HTTP 403 / 429 발생 시:
  - 즉시 크롤링 중단
  - 다음 스케줄로 이월

---

## 10. 설계 원칙 요약

- robots.txt + sitemap을 **의도 존중 신호**로 해석한다
- raw는 “사실 원재료”, 의미 부여는 후단 파이프라인 책임
- 느리고 보수적으로 접근한다
- 다시 수집 가능하도록 설계한다

---

## 11. 절대 하면 안 되는 것 (Red Lines)

- 네이버 뉴스 본문 크롤링
- robots.txt 무시
- AI 봇 User-Agent 사칭
- 기사 전문 무제한 저장
- raw 데이터를 API 응답으로 노출

이 중 하나라도 위반하면 **설계 위반**이다.