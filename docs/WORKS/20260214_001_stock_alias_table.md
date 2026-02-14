# stock_alias 테이블 생성

## 목표

뉴스에서 추출된 키워드를 종목에 매핑할 때, 정식 종목명 외에 별칭/약칭으로도 매칭할 수 있도록 별칭 테이블을 추가한다.

## 배경

현재 뉴스 키워드 → 종목 매핑은 `stock_master.name_kr_short`와 exact match로만 동작한다. "삼전", "LG엔솔", "현차" 같은 흔한 별칭은 매칭되지 않는 문제가 있다.

별칭 테이블을 만들어서, exact match 실패 시 `stock_alias.alias`에서 추가 검색하는 구조를 만든다.

## 할 것

### Flyway 마이그레이션 (V16)

```sql
CREATE TABLE stock_alias (
    alias TEXT PRIMARY KEY,
    stock_code TEXT NOT NULL,
    stock_name TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_stock_alias_stock_code ON stock_alias(stock_code);
```

- `alias`가 PK (검색 기준이 alias이므로)
- `stock_name`은 데이터 검증 편의용 (FK 아님)
- `stock_code`도 FK 걸지 않음 (stock_master와 느슨한 연결)

## 안 할 것

- 초기 데이터 INSERT (수작업으로 진행)
- Entity, Repository (매칭 로직 구현 시 별도 작업)
- Admin API/UI
- stock_master와의 FK 제약조건
