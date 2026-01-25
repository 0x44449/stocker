# stocker – KRX 전체 아키텍처 설계

## 0️⃣ 설계 목표 (한 줄)

> **KRX의 불안정한 외부 구조를 내부에서 완전히 캡슐화하고,
> 로그인 → 세션 → 기능별 크롤러를 단계적으로 확장한다.**

---

## 1️⃣ KRX 아키텍처의 큰 그림

```
[ Batch / UseCase ]
        ↓
[ KRX Application Layer ]
        ↓
[ KRX Session Layer ]
        ↓
[ KRX HTTP / Transport ]
        ↓
[ KRX External (data.krx.co.kr) ]
```

📌 핵심 원칙

* **위에서 아래로만 의존**
* KRX 특유의 더러움은 **아래로 내려갈수록만 존재**

---

## 2️⃣ 패키지 전체 구조 (KRX 전용)

```
ingest
 └─ krx
     ├─ auth        // 로그인 & 세션
     ├─ session     // 인증된 실행 흐름
     ├─ request     // KRX 요청 정의
     ├─ fetcher     // 기능별 데이터 수집
     ├─ parser      // 응답 파싱
     └─ support     // 공통 유틸
```

---

## 3️⃣ auth 계층 – 로그인 전용 (이미 확정)

```
auth
 ├─ KrxAuthClient
 ├─ KrxLoginRequestBuilder
 ├─ KrxLoginResponse
 ├─ KrxSession
 ├─ KrxSessionProvider
 └─ KrxLoginException
```

### 책임

* 로그인 수행
* JSESSIONID 확보
* KrxSession 생성

📌 **여기서는 “로그인”까지만**

* 데이터 요청 ❌
* CSV ❌

---

## 4️⃣ session 계층 – 인증 상태 관리 (핵심 허브)

```
session
 ├─ KrxSession
 ├─ KrxSessionValidator
 ├─ KrxSessionProvider   ← auth와 연결
 └─ KrxAuthenticatedExecutor
```

### 역할

* 세션 유효 여부 판단
* 세션 기반 요청 실행
* (나중에) 세션 만료 시 재로그인

### 핵심 인터페이스

```java
interface KrxAuthenticatedExecutor {
    <T> T execute(KrxSession session, KrxRequest<T> request);
}
```

📌 **모든 KRX 요청은 이걸 통해서만 나간다**

---

## 5️⃣ request 계층 – “KRX 요청 명세”

```
request
 ├─ KrxRequest<T>
 ├─ KrxRequestBuilder
 └─ impl
     ├─ DailyPriceRequest
     ├─ StockListRequest
     └─ ...
```

### 책임

* endpoint
* HTTP method
* form-data 파라미터
* 응답 타입

📌 포인트

* **KRX 파라미터 하드코딩 금지**
* bld, mktId, 날짜 포맷 전부 여기

---

## 6️⃣ fetcher 계층 – 기능별 수집 로직

```
fetcher
 ├─ KrxDailyPriceFetcher
 ├─ KrxStockListFetcher
 └─ (later)
     ├─ KrxDisclosureFetcher
     ├─ KrxInvestorFetcher
```

### 역할

* “무엇을 수집한다”는 비즈니스 의미
* request 조립
* executor 호출

📌 fetcher는:

* HTTP 모름
* 쿠키 모름
* 로그인 모름

---

## 7️⃣ parser 계층 – 응답 정제

```
parser
 ├─ KrxCsvParser
 ├─ KrxJsonParser
 └─ mapper
     ├─ DailyPriceMapper
     └─ StockMapper
```

### 역할

```
KRX Raw Response
 → Raw DTO
 → Domain-friendly DTO
```

📌 절대 금지

* raw CSV를 domain으로 바로 변환
* 컬럼 index 하드코딩 분산

---

## 8️⃣ support 계층 – 공통부

```
support
 ├─ KrxConstants
 ├─ KrxDateFormatter
 ├─ KrxException
 └─ RetryPolicy
```

---

## 9️⃣ 실제 호출 흐름 (로그인 이후)

### 예: 일봉 수집

```
Batch Job
 └─ KrxDailyPriceFetcher
     └─ KrxAuthenticatedExecutor
         └─ KrxSessionProvider.getSession()
             └─ (필요 시 로그인)
         └─ KrxAuthClient (HTTP)
             └─ KRX
```

📌 로그인 / 세션 / HTTP는 **전부 자동으로 감춰짐**
