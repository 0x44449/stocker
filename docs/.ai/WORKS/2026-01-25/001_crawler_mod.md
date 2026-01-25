# [Immediate Fixes] 뉴스 크롤링 안정화를 위한 필수 수정 사항

본 문서는 현재 구현된 뉴스 크롤러를 **운영 가능한 상태로 만들기 위해**
지금 당장 적용해야 할 최소한의 구조 수정 사항을 정리한다.

목표는 다음과 같다:
- API 서버 역할과 크롤링 Job 역할의 명확한 분리
- 크롤링으로 인한 API 서버 병목 방지
- 중복 실행으로 인한 데이터/리소스 문제 예방

---

## 1️⃣ 크롤링 실행 로직을 REST 밖으로 분리

### 문제점 (현재 상태)

- 크롤링의 주 실행 진입점이 REST Controller에 있음
- 크롤링은:
  - 장시간 실행
  - 사용자 요청과 무관
  - 재시도/중복 실행 위험 있음

REST API는 **운영 트리거**로는 가능하나  
**실제 실행 경로로 두는 것은 부적합**하다.

---

### 수정 목표

- 크롤링 실행은 **Service / Job 계층**에서 담당
- REST API는:
  - 테스트용
  - 관리자 수동 트리거용
  - 내부 호출용

---

### 권장 구조

```text
CrawlerController (optional / internal)
        ↓
NewsCrawlJobService   ← 크롤링 실행의 유일한 진입점
        ↓
NewsCrawlEngine
        ↓
Provider / Engine 로직
````

---

### 적용 지침

* `CrawlerController`는 크롤링 로직을 직접 호출하지 않는다
* 모든 실행은 `NewsCrawlJobService.run()` 같은 메서드를 통해 수행
* 이후 Scheduler는 REST를 거치지 않고 JobService를 직접 호출

---

## 2️⃣ Scheduler 전용 Thread Pool 분리

### 문제점

* 기본 `@Scheduled` 설정은:

  * API 서버와 동일한 JVM 리소스 사용
  * 설정 없이 사용 시 스레드 관리가 불명확

크롤링은:

* 느리고
* IO-bound이며
* API 요청과 경쟁하면 안 된다

---

### 수정 목표

* 크롤링 전용 스케줄러 스레드 풀 분리
* API 처리 스레드와 논리적/구조적으로 분리

---

### 적용 지침 (필수)

#### 전용 TaskScheduler Bean 생성

```java
@Bean
public TaskScheduler crawlTaskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(1); // 초기에는 반드시 1
    scheduler.setThreadNamePrefix("news-crawl-");
    scheduler.initialize();
    return scheduler;
}
```

#### Scheduler 적용

```java
@Scheduled(
    cron = "...",
    scheduler = "crawlTaskScheduler"
)
public void runScheduledCrawl() {
    newsCrawlJobService.run();
}
```

---

### 주의사항

* poolSize는 **초기에는 절대 늘리지 않는다**
* 병렬 크롤링은 v1 이후에만 고려

---

## 3️⃣ 동시 실행 방지 (락 1개)

### 문제점

아래 상황에서 **중복 실행 위험**이 있다:

* 서버 재시작 직후
* 스케줄러 + 수동 REST 호출 동시 발생
* 배포 중 스케줄 겹침

중복 실행 시:

* 동일 뉴스 중복 수집
* DB/외부 사이트 부하 증가
* 예측 불가능한 상태 발생

---

### 수정 목표

* **동시에 1개의 크롤링만 실행 가능**
* 이미 실행 중이면:

  * 즉시 종료하거나
  * skip 처리

---

### 권장 방식 (v0)

#### DB 기반 락 (권장)

* `crawl_lock` 같은 단일 row 테이블 사용
* 실행 시:

  * lock 획득 시만 진행
  * 실패 시 즉시 종료

개념 예시:

```text
tryAcquireLock("NEWS_CRAWL")
    → 성공: 실행
    → 실패: skip
```

---

### 대안 (단일 인스턴스 한정)

* JVM in-memory flag
* 단, **멀티 인스턴스 확장 시 즉시 제거 대상**

---

### 적용 지침

* 락은 `NewsCrawlJobService` 진입부에서 획득
* Engine / Provider는 락 개념을 몰라야 한다
* finally 블록에서 반드시 해제

---

## 요약 (반드시 지킬 것)

1. 크롤링 실행 로직은 REST 밖으로 이동
2. Scheduler는 전용 thread pool 사용
3. 크롤링은 항상 단일 실행 보장
