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
