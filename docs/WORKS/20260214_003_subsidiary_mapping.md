# subsidiary_mapping 테이블 생성 및 클러스터링 연동

## 목적

뉴스에 비상장 자회사명(삼성디스플레이, SK온, 카카오모빌리티 등)이 등장하면 매칭할 종목이 없어서 누락된다.
비상장 자회사 → 상장 모회사로 연결하는 매핑 테이블을 추가하고, 클러스터링에서 stock_alias와 함께 참조한다.

## 결정사항

### 테이블 (Flyway V17)

- 컬럼: subsidiary_name(TEXT, PK), stock_code(TEXT), stock_name(TEXT), created_at(TIMESTAMPTZ, DEFAULT now())
- FK 없음, stock_name은 검증 편의용
- stock_code에 인덱스 추가

### 클러스터링 연동

- stock_alias와 동일한 방식으로 클러스터링 검색 키워드에 추가
- 종목명 + alias + subsidiary_name 모두 OR 조건으로 검색

### 범위

- 비상장 자회사만 대상. 상장 자회사는 자기 종목이 있으므로 제외
- 초기 데이터는 수작업으로 적재

## 안 할 것

- 초기 데이터 INSERT
- 상장 자회사 → 모회사 연결
- 관계 변경 이력 관리
- 다단계 자회사 자동 처리
