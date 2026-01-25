# 크롤링 로그 출력 형식 원칙 (One-line Logging)

본 문서는 뉴스 크롤러 로그의 **출력 형식 규칙**을 정의한다.  
로그는 기본적으로 **한 줄(one-line)** 로 출력하는 것을 원칙으로 하며,  
예외는 **중대한 오류로 stack trace가 필요한 경우**로 제한한다.

---

## 1️⃣ 기본 원칙

### ✅ One-line Logging 원칙

- 모든 정상/경고 로그는 **반드시 1줄**
- 줄바꿈(`\n`) 사용 금지
- key=value 형태로 이어서 출력

이유:
- grep / awk / jq / Loki / CloudWatch 등
- 대부분의 로그 도구가 **라인 단위 처리**를 전제로 함
- 멀티라인 로그는 파싱·집계·필터링에 불리

---

## 2️⃣ 권장 로그 포맷

### 기본 형태

```text
event=NEWS_CRAWL_STARTED jobId=20240110T030000 providers=3
````

```text
event=PROVIDER_SUCCESS jobId=20240110T030000 provider=mk fetched=12 saved=10
```

```text
event=ARTICLE_SKIPPED jobId=20240110T030000 provider=mk reason=DUPLICATE
```

### 규칙

* 공백으로 필드 구분
* `key=value` 형식 고정
* 메시지 문장형 로그 지양

❌

```text
뉴스 크롤링이 시작되었습니다. provider=mk
```

⭕

```text
event=PROVIDER_START jobId=... provider=mk
```

---

## 3️⃣ Job 요약 로그 (중요)

### 종료 요약 (1줄)

```text
event=NEWS_CRAWL_FINISHED jobId=20240110T030000 durationMs=128432 providers=3 fetched=42 saved=35 skipped=7
```

이 로그 **1줄만으로도**:

* 실행 여부
* 성능
* 수집량
  파악 가능해야 한다.

---

## 4️⃣ WARN 로그도 반드시 1줄

```text
event=PROVIDER_SKIPPED jobId=... provider=mk reason=HTTP_429 action=SKIP_PROVIDER
```

```text
event=ARTICLE_SKIPPED jobId=... provider=mk reason=EMPTY_CONTENT
```

❌ 원인 설명을 여러 줄로 풀어쓰지 않는다
❌ 예외 메시지를 그대로 출력하지 않는다

---

## 5️⃣ ERROR 로그 예외 규칙 (유일한 멀티라인 허용)

### ❗ 멀티라인 허용 조건

아래 **모두** 만족할 때만 허용:

* Job 자체가 중단됨
* 복구 불가 또는 즉시 조치 필요
* stack trace가 실제로 의미 있음

---

### ERROR 로그 출력 방식

#### 1️⃣ 요약 로그 (1줄, 필수)

```text
event=NEWS_CRAWL_ERROR jobId=20240110T030000 reason=DB_CONNECTION_FAILED
```

#### 2️⃣ stack trace (선택, multi-line)

```text
java.sql.SQLException: Connection refused
    at ...
    at ...
```

👉 **항상 요약 1줄 로그가 먼저** 나오고
👉 그 다음에만 stack trace 출력

---

## 6️⃣ Logger 사용 지침 (코드 레벨)

### 기본 로그

```java
crawlLogger.info(
  "event=PROVIDER_SUCCESS jobId={} provider={} fetched={} saved={}",
  jobId, providerId, fetched, saved
);
```

### ERROR 로그

```java
crawlLogger.error(
  "event=NEWS_CRAWL_ERROR jobId={} reason={}",
  jobId, reason, exception
);
```

* message는 1줄
* exception은 stack trace 용도로만 사용

---

## 7️⃣ 절대 금지 사항

* ❌ 여러 줄에 걸친 설명 로그
* ❌ JSON pretty-print
* ❌ HTML / selector / 본문 출력
* ❌ 예외 메시지를 WARN/INFO에 그대로 출력

---

## 8️⃣ 이 원칙의 장점

* grep 한 줄로 분석 가능
* 로그 수집 시스템 이전 용이
* 장애 시 빠른 원인 파악
* 로그 용량 예측 쉬움
