# 뉴스 크롤링 로그 설계안 (MVP)

본 문서는 뉴스 크롤링 기능의 **로그 설계 기준**을 정의한다.  
목표는 다음과 같다:

- 크롤링 실행 상태를 빠르게 파악할 수 있을 것
- 장애/실패 원인을 로그만 보고 추적 가능할 것
- 로그 과다로 인한 노이즈를 방지할 것
- 향후 로그 수집 시스템(ELK 등)으로 이전이 쉬울 것

---

## 1️⃣ 로그 기본 원칙

### 로그의 역할
크롤링 로그는 다음 질문에 답해야 한다:

1. 언제 실행되었는가
2. 어떤 Provider가 실행되었는가
3. 성공/실패 여부는 무엇인가
4. 왜 실패했는가
5. 이번 실행에서 무엇을 얼마나 수집했는가

이 외의 정보는 **로그 대상이 아니다.**

---

### 로그 레벨 사용 원칙

| Level | 사용 기준 |
|----|----|
| INFO | Job/Provider 실행 요약 |
| WARN | Provider/Article 단위 부분 실패 |
| ERROR | Job 중단 수준의 치명적 실패 |
| DEBUG | 개발/디버깅 전용 (기본 OFF) |

❌ 크롤링 성공 1건당 INFO 로그 금지  
❌ HTML 전문, selector 정보 로그 금지  

---

## 2️⃣ 로그 출력 방식

### 저장 위치
- **파일 로그**
- API 로그와 분리된 **크롤링 전용 로그 파일**

예시:
```text
logs/
 ├─ app.log
 └─ crawl.log
````

---

### Logger 구분

* 크롤링 전용 Logger 이름: `CRAWL`
* 패키지 기준: `com.hanzi.stocker.ingest.news`

로그 레벨은 **설정(application.yml)으로 제어 가능**해야 한다.

```yaml
logging:
  level:
    CRAWL: INFO
    com.hanzi.stocker.ingest.news: INFO
```

---

## 3️⃣ Job 단위 로그 (필수)

### Job 시작 로그

```text
[NEWS-CRAWL] started
jobId=20240110T030000
providers=3
```

* jobId는 실행 단위 식별자
* 이후 모든 로그에 jobId 포함

---

### Job 종료 로그 (요약)

```text
[NEWS-CRAWL] finished
jobId=20240110T030000
durationMs=128432
providers=3
articlesFetched=42
articlesSaved=35
articlesSkipped=7
```

👉 이 로그 **1줄만 보고도 실행 결과 파악 가능**해야 한다.

---

## 4️⃣ Provider 단위 로그

### Provider 시작

```text
[PROVIDER] start
jobId=...
provider=mk
```

---

### Provider 정상 종료

```text
[PROVIDER] success
jobId=...
provider=mk
articlesFetched=12
articlesSaved=10
```

---

### Provider 부분 실패 (WARN)

```text
[PROVIDER] skipped
jobId=...
provider=mk
reason=HTTP_429
action=SKIP_PROVIDER
```

가능한 reason 예:

* ROBOTS_DENY
* HTTP_403
* HTTP_429
* SITEMAP_FETCH_FAILED
* PARSE_FAILED

❌ stacktrace 출력 금지
❌ 예외 전체 출력 금지

---

## 5️⃣ 기사(Article) 단위 로그

### 기본 원칙

* 기사 단위 **성공 로그는 남기지 않는다**
* 실패/스킵만 로그로 남긴다

---

### 기사 스킵 (WARN)

```text
[ARTICLE] skipped
jobId=...
provider=mk
reason=EMPTY_CONTENT
```

```text
[ARTICLE] skipped
jobId=...
provider=mk
reason=DUPLICATE
```

---

## 6️⃣ ERROR 로그 기준 (엄격)

### ERROR로 남길 경우

* 크롤링 Job 자체 중단
* DB 연결 실패
* 락 획득 실패 (비정상 상태)
* 모든 Provider가 실패한 경우

```text
[NEWS-CRAWL] error
jobId=...
reason=DB_CONNECTION_FAILED
```

❌ Provider 단일 실패는 ERROR 금지
❌ 기사 단일 실패는 ERROR 금지

---

## 7️⃣ 로그 필드 공통 규칙

모든 크롤링 로그는 아래 필드를 **가능한 한 포함**한다:

* jobId
* provider (해당 시)
* event (started / success / skipped / error)
* reason (해당 시)
* durationMs (종료 시)

이 규칙을 지키면:

* 로그 수집 시스템으로 이전이 쉬움
* 단순 grep만으로도 분석 가능

---

## 8️⃣ 로그 위치 책임 분리

| 위치                  | 로그 책임          |
| ------------------- | -------------- |
| NewsCrawlJobService | Job 시작/종료      |
| NewsCrawlEngine     | Provider 실행 흐름 |
| Provider            | 정책/파싱 실패       |
| RawService          | 저장 실패          |

❌ Provider에서 Job 전체 요약 로그 금지
❌ Engine에서 Article 상세 로그 금지

---

## 9️⃣ 선택 사항 (권장)

### Job 종료 시 JSON 요약 로그 (1줄)

```json
{
  "job": "NEWS_CRAWL",
  "jobId": "20240110T030000",
  "providers": 3,
  "saved": 35,
  "skipped": 7,
  "durationMs": 128432
}
```

* 사람 + 머신 모두 읽기 가능
* 향후 메트릭화 용이

---

## 10️⃣ 절대 하지 말아야 할 것

* DEBUG 로그로 운영
* HTML / selector 출력
* Provider마다 제각각 로그 포맷
* 기사 성공 로그 남발
