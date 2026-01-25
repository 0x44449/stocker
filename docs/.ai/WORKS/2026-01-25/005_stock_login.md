# KRX 로그인 설계 확정 (stocker)

## 1️⃣ 로그인 방식 요약 (팩트 기준)

### 로그인 엔드포인트

```
POST https://data.krx.co.kr/contents/MDC/COMS/client/MDCCOMS001D1.cmd
```

### Content-Type

```
application/x-www-form-urlencoded
```

### Request Body 파라미터

| 필드명         | 사용 여부 | 값          |
| ----------- | ----- | ---------- |
| mbrNm       | ❌     | 빈 값        |
| telNo       | ❌     | 빈 값        |
| di          | ❌     | 빈 값        |
| certType    | ❌     | 빈 값        |
| **mbrId**   | ✅     | 로그인 ID     |
| **pw**      | ✅     | 로그인 비밀번호   |
| **skipDup** | ✅     | `"Y"` (고정) |

👉 **실제 의미 있는 필드는 3개뿐**

* `mbrId`
* `pw`
* `skipDup=Y`

---

### 성공 응답 (JSON)

```json
{
  "previousMemberYn": false,
  "MDC_MBR_TP_CD": "P",
  "MBR_NO": "1000005089",
  "_error_code": "CD001",
  "_error_message": "정상"
}
```

### 성공 판별 기준

* `_error_code == "CD001"`
* `_error_message == "정상"`

### 세션 발급

* `Set-Cookie` 헤더로 **JSESSIONID** 전달됨
* 이 JSESSIONID가 이후 authenticated 요청의 핵심

---

## 2️⃣ stocker 기준 로그인 책임 분리 (확정)

```
ingest
 └─ krx
     └─ auth
         ├─ KrxAuthClient
         ├─ KrxLoginRequestBuilder
         ├─ KrxLoginResponse
         ├─ KrxSession
         ├─ KrxSessionProvider
         └─ KrxLoginException
```

---

## 3️⃣ 컴포넌트별 역할 확정

### 3.1 KrxLoginRequestBuilder

**책임**

* x-www-form-urlencoded body 생성
* 사용하지 않는 필드는 **명시적으로 빈 값** 처리

**출력**

* `MultiValueMap<String, String>`

📌 하드코딩 금지 영역

* 필드명
* skipDup 값

---

### 3.2 KrxAuthClient

**책임**

* WebClient 래핑
* 쿠키 자동 유지
* Set-Cookie에서 JSESSIONID 수신

📌 주의

* redirect 허용
* response body + headers 모두 접근 가능해야 함

---

### 3.3 KrxLoginResponse (DTO)

로그인 응답 JSON 전용 DTO

필드:

* previousMemberYn
* MDC_MBR_TP_CD
* MBR_NO
* _error_code
* _error_message

📌 이 DTO는 **auth 패키지 밖으로 나가지 않음**

---

### 3.4 KrxSession (핵심 값 객체)

**포함 정보**

* sessionId (JSESSIONID)
* memberNo (MBR_NO)
* createdAt
* lastValidatedAt

📌 절대 금지

* String cookie 직접 전달
* Map<String,String> 형태 세션

---

### 3.5 KrxSessionProvider (단일 진입점)

**역할**

* 로그인 수행
* 세션 생성
* 성공/실패 판별

**흐름**

```
login()
 ├─ requestBuilder.build()
 ├─ authClient.post()
 ├─ response._error_code 검증
 ├─ Set-Cookie에서 JSESSIONID 추출
 └─ KrxSession 생성
```

---

## 4️⃣ 실패 케이스 설계 (지금 반드시 필요)

### KrxLoginException 타입

* INVALID_CREDENTIALS
  → _error_code != CD001
* SESSION_NOT_ISSUED
  → 로그인 성공 JSON인데 JSESSIONID 없음
* UNKNOWN_RESPONSE
  → JSON 구조 변경 / 파싱 실패

👉 이후 모듈은
**“로그인 실패했다”**만 알면 됨
(원인 분기는 auth 내부에서 끝)
