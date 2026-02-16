# 작업: AuthService JWT 알고리즘 자동 선택

## 목표

AuthService의 JWT 검증에서 RSA256 하드코딩을 제거하고, JWKS에서 가져온 키의 알고리즘에 따라 자동으로 검증 방식을 선택한다.

## 배경

- 현재 코드: `Algorithm.RSA256()` 하드코딩
- 실제 Supabase 프로젝트: ECC (P-256) / ES256 사용 중
- 현재 상태로는 JWT 검증 실패 (401 반환)

## 할 것

`AuthService.verifyAndGetUid()` 메서드에서 알고리즘 자동 선택:

```java
Jwk jwk = jwkProvider.get(decoded.getKeyId());
Algorithm algorithm = switch (jwk.getAlgorithm()) {
    case "ES256" -> Algorithm.ECDSA256((ECPublicKey) jwk.getPublicKey(), null);
    case "RS256" -> Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
    default -> throw new IllegalArgumentException("지원하지 않는 알고리즘: " + jwk.getAlgorithm());
};
```

- ES256 (ECC P-256): 현재 Supabase 프로젝트에서 사용 중
- RS256 (RSA): 향후 키 로테이션 대비
- HS256: JWKS에 공개키가 노출되지 않으므로 미지원 (Supabase도 비대칭키 전환 권장)

## 안 할 것

- HS256 지원 (대칭키는 JWKS로 제공되지 않음)
- 그 외 변경 없음

## 관련 파일

| 파일 | 이유 |
|------|------|
| `AuthService.java` | 알고리즘 선택 로직 변경 |
