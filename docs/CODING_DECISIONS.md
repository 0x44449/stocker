# Coding Decisions

코드 작업 시 결정된 코딩 스타일, 설계 원칙, 기술적 선택을 기록합니다.
architect, coder, reviewer 모두 이 결정들을 참고하여 일관된 코드를 작성합니다.

---

## JPA

### Upsert는 saveAll()로 처리

**결정일**: 2026-02-01

**배경**:
- Native SQL upsert(`ON CONFLICT DO UPDATE`)는 컬럼명 하드코딩 필요
- Entity의 `@Column` 매핑과 이중 관리 발생

**결정**:
- 소규모 데이터(수천 건 이하, 하루 1회)는 `repository.saveAll()` 사용
- JPA가 SELECT → INSERT/UPDATE로 처리하더라도 성능 차이 무의미
- 다른 크롤러들과 일관성 유지

**예외**:
- 수만 건 이상 대량 데이터, 빈번한 실행 시 Native upsert 검토

---

## 파싱

### 예외 케이스는 별도 함수로 분리

**결정일**: 2026-02-01

**배경**:
- 종목 마스터 CSV의 `액면가` 컬럼에 "무액면" 문자열 존재
- 범용 `parseLong()`에 try-catch 추가 시 다른 컬럼의 버그 감지 불가

**결정**:
- `parseLong()` - 엄격한 버전 (예외 발생, 기본)
- `parseLongOrNull()` - 느슨한 버전 (특수 케이스용)
- 사용 시 주석으로 이유 명시

**예시**:
```java
// 액면가: 투자회사/펀드 등은 "무액면"으로 표기되어 숫자가 아닌 값은 null 처리
parseLongOrNull(record.get("액면가")),
parseLong(record.get("상장주식수"))  // 엄격하게 검증
```

---

## 쿼리

### 동적 쿼리는 QueryDSL 사용

**결정일**: 2026-02-01

**배경**:
- 동적 필터, 집계, JOIN이 필요한 쿼리가 증가할 예정
- Native SQL을 코드에 직접 박는 것은 유지보수가 어려움
- JPA 메서드 네이밍으로는 GROUP BY, HAVING 등 표현 불가

**결정**:
- 동적 쿼리가 필요한 경우 QueryDSL 사용
- 단순 조회(findByXxx 등)는 JPA Repository 메서드 사용
- `@Query` native SQL은 사용하지 않음

---

### DB 조회와 비즈니스 로직 분리

**결정일**: 2026-02-01

**배경**:
- DB에서 집계(CASE WHEN, SUM 등)까지 처리하면 쿼리가 과도하게 복잡해짐
- 비즈니스 로직이 쿼리에 섞이면 가독성과 유지보수성 저하

**결정**:
- DB에서는 페이징된 ID 조회 + 해당 ID의 원본 데이터 조회만 수행
- 집계, status 계산 등 비즈니스 로직은 서비스 레이어에서 처리
- 단, 페이징에 필요한 필터 조건(HAVING 등)은 DB에서 처리

---

## 의존성

### springdoc-openapi 3.0.1 사용

**결정일**: 2026-02-01

**배경**:
- Spring Boot 4.0 + QueryDSL 조합에서 springdoc-openapi 2.8.4 사용 시 빈 생성 실패
- `queryDslQuerydslPredicateOperationCustomizer` 빈 생성 오류 발생
- springdoc 3.0.1이 Spring Boot 4.0 정식 지원

**결정**:
- springdoc-openapi 3.0.1 사용
- 2.x 버전은 Spring Boot 4.0과 호환 안 됨

**예외**:
- 없음

---
