# 클러스터링 결과 저장 및 스케줄러

## 목적

클러스터링을 매번 실시간으로 돌리면 느리다. 결과를 DB에 저장하고, 새 뉴스가 있을 때만 재실행하여 피드 API는 저장된 결과를 읽기만 하도록 한다.

## 결정사항

### 테이블 (Flyway 마이그레이션)

```sql
CREATE TABLE stock_cluster_result (
    stock_code TEXT NOT NULL,
    stock_name TEXT NOT NULL,
    total_count INT NOT NULL,
    input_hash TEXT NOT NULL,
    clustered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    result JSONB NOT NULL,
    PRIMARY KEY (stock_code, clustered_at)
);
```

- 변화가 있으면 무조건 새 행으로 쌓임 (같은 날이라도)
- 피드 조회: 종목별 가장 최근 clustered_at 기준
- input_hash: 입력 news_id 목록을 정렬 후 md5. 가장 최근 행의 input_hash와 비교하여 변화 감지

### 클러스터링 시간 범위

- 최근 24시간

### 실행 방식 (Python news-analyzer에서 수행)

1. **스케줄러**: 1시간마다 자동 실행
2. **수동 API**: FastAPI 엔드포인트로 즉시 실행 가능

### 중복 실행 방지

- 실행 중일 때 스케줄러든 수동이든 중복 실행되지 않아야 함
- 방법: in-memory 플래그로 제어
- 이미 실행 중이면 스케줄러는 스킵, 수동 API는 "이미 실행 중" 응답 반환

### 실행 흐름

- 대상: extraction에서 최근 24시간 내 멘션 있는 종목 (alias, subsidiary 포함 정규화)
- **전제조건: stock_master에 존재하는 종목만 대상** (정규화 결과 stock_code가 stock_master에 있어야 함)
- 종목별로:
  1. 해당 종목 extraction news_id 조회 → 정렬 → md5
  2. stock_cluster_result에서 해당 종목 가장 최근 행의 input_hash 비교
  3. 다르면 클러스터링 실행 → 새 행 INSERT
  4. 같으면 스킵

### 피드 API 변경 (Spring)

- 기존: Python 클러스터링 API 실시간 호출
- 변경: stock_cluster_result 테이블에서 최신 결과 조회하여 반환
- Python 호출 불필요 (DB만 읽음)

## 안 할 것

- 분산 락 (단일 인스턴스이므로 in-memory로 충분)
- Spring에서 클러스터링 실행 (Python에서만 수행)
- 초기 전체 데이터 일괄 생성 (수동 API로 처리)
- 클러스터링 로직 변경
- 주말/공휴일 특별 처리
