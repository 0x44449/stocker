# 작업: Supabase 인증 연동

## 목표

Supabase 소셜 로그인 기반 인증을 구현한다.
- 클라이언트: Supabase JWT를 Authorization 헤더에 담아서 API 호출
- 서버: JWT 검증 → uid 추출 → 허용 사용자 확인
- 허용되지 않은 사용자는 API 사용 불가

## 배경

### 인증 흐름
```
[Expo 앱] → Supabase 소셜 로그인 → JWT 획득
    ↓
GET /api/auth/me (Authorization: Bearer <jwt>)
    ↓
서버: JWT 검증 → uid 추출 → allowed_user 테이블 조회
    ↓
  ┌─────┴─────┐
허용됨        미허용
  │            │
APPROVED    PENDING
  │            │
앱 진입    "인증 대기중" 화면
```

### 허용 사용자 관리
- 테스트 단계이므로 허용된 사용자 uid를 `allowed_user` 테이블에 수동 INSERT
- 관리자 UI 없음. DB 직접 조작.

---

## 할 것

### Phase 1: DB

**Flyway 마이그레이션 추가** (`V19__add_allowed_user.sql`)

```sql
CREATE TABLE allowed_user (
    uid TEXT PRIMARY KEY,
    memo TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

- `uid`: Supabase user의 `sub` 값 (UUID 문자열)
- `memo`: 누구인지 메모용 ("zina" 등). 필수 아님.

### Phase 2: JWT 검증 + 인증 필터

**의존성 추가**
- `com.auth0:java-jwt` — JWT 파싱/검증
- `com.auth0:jwks-rsa` — Supabase JWKS 공개키 자동 조회

**Supabase JWT 검증 방식**
- JWKS 방식 사용. Supabase JWKS endpoint(`https://<project-ref>.supabase.co/.well-known/jwks.json`)에서 공개키 자동 조회.
- 키 로테이션 시 서버 재배포 없이 자동 대응.
- JWT 서명 검증 + 만료 확인 → `sub` 클레임에서 uid 추출

**인증 필터 (HandlerInterceptor)**
- `/api/**` 전체에 인터셉터 등록
- 기본: 모든 API는 JWT 검증 + 허용 사용자 확인
- 예외: `@AllowPublic` 어노테이션이 붙은 메서드는 인증 스킵
- `@Authenticated` 어노테이션이 붙은 메서드는 JWT 검증만 (허용 사용자 확인 안함)
- 인증 실패: 401 + `{"error":"UNAUTHORIZED","message":"INVALID_TOKEN"}`, 미허용: 403 + `{"error":"FORBIDDEN","message":"USER_NOT_ALLOWED"}`
- 응답 포맷은 `GlobalExceptionHandler`의 `ErrorResponse`와 동일하게 통일

**커스텀 어노테이션**
```java
@AllowPublic         // 인증 없이 접근 가능
@Authenticated       // JWT 검증만 (허용 사용자 확인 안함)
// 어노테이션 없음  // JWT 검증 + 허용 사용자 확인 (기본)
```

**적용 예시**
```java
@Authenticated
@GetMapping("/me")
public AuthMeResponse me(...) { }  // JWT만 검증, 허용 확인 안함

@GetMapping
public WatchlistResponse getWatchlist(...) { }  // JWT + 허용 확인 (기본)
```

**application.yaml 설정 추가**
```yaml
supabase:
  url: ${SUPABASE_URL}  # https://<project-ref>.supabase.co (JWKS endpoint 구성용)
```

### Phase 3: API

**`GET /api/auth/me`**

요청: `Authorization: Bearer <supabase-jwt>`

응답:
```json
// 허용된 사용자
{ "status": "APPROVED", "uid": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" }

// 미허용 사용자
{ "status": "PENDING" }
```

- JWT가 유효하면 무조건 200 응답 (APPROVED 또는 PENDING)
- JWT가 유효하지 않으면 401

### Phase 4: 인증된 사용자 정보 전달

- 인증 필터에서 검증된 uid를 요청 attribute에 저장
- 컨트롤러에서 uid를 꺼내 쓸 수 있도록

```java
// 필터에서
request.setAttribute("uid", uid);

// 컨트롤러에서
@RequestAttribute("uid") String uid
```

---

## 안 할 것

- Spring Security 도입 (HandlerInterceptor로 충분)
- 회원가입 화면/API (Supabase가 처리)
- 관리자 허용 UI (DB 직접 INSERT)
- 토큰 갱신 API (Supabase SDK가 클라이언트에서 처리)
- 사용자 프로필 테이블 (지금은 uid만 있으면 됨)

---

## 파일 구조 (예상)

```
api/
├── config/
│   ├── AuthInterceptor.java          # JWT 검증 + 허용 사용자 확인 (어노테이션 기반)
│   ├── AllowPublic.java               # @AllowPublic 어노테이션
│   ├── Authenticated.java             # @Authenticated 어노테이션
│   └── WebConfig.java                # 인터셉터 등록 (기존 파일에 추가)
├── api/
│   └── auth/
│       ├── AuthController.java       # GET /api/auth/me
│       └── AuthService.java          # JWT 파싱, allowed_user 조회
├── entities/
│   └── AllowedUserEntity.java
├── repositories/
│   └── AllowedUserRepository.java
└── resources/
    └── db/migration/
        └── V19__add_allowed_user.sql
```

---

## 관련 파일

| 파일 | 이유 |
|------|------|
| `build.gradle` | JWT 라이브러리 의존성 추가 |
| `application.yaml` | supabase 설정 추가 |
| `WebConfig.java` | 인터셉터 등록 |
| `GlobalExceptionHandler.java` | 인증 에러 핸들링 (필요 시) |

---

## 열린 질문

없음
