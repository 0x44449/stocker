# 헤드라인 API — stock_master 매칭 필터 추가

## 배경

현재 헤드라인 API가 extraction keywords를 그대로 카운팅하고 있어서, 종목과 무관한 키워드도 포함된다.
stock_master에 있는 종목명과 매칭되는 키워드만 카운팅하도록 변경한다.
또한 현재 뉴스와 extraction을 전부 메모리에 올려서 처리하고 있어 느리므로, DB에서 필터링 가능한 부분은 QueryDSL로 처리한다.

## 목표

1. DB에서 필요한 데이터만 가져오도록 쿼리 최적화
2. stock_master 매칭 필터 추가

## 관련 파일

- `apps/api/src/main/java/com/hanzi/stocker/api/headline/HeadlineService.java`

## 관련 테이블

```
news_raw          - id, published_at, title, ...
news_extraction   - extraction_id, news_id, keywords (JSONB), llm_model, prompt_version
stock_master      - stock_code, name_kr, name_kr_short, ...
```

## 할 것

### HeadlineService 수정

DB에서 처리 (QueryDSL):
1. news_raw에서 해당 date의 news_id 목록 조회
2. news_extraction에서 해당 news_id들 + llm_model="qwen2.5:7b" + prompt_version="v1" + keywords가 빈 배열이 아닌 것만 조회

Java에서 처리:
3. stock_master 전체 조회 → name_kr, name_kr_short를 Set으로 구성 (~950건)
4. extraction의 keywords를 풀면서 Set에 있는 것만 카운팅
5. threshold 이상인 종목만 선정, count 내림차순

응답 형태는 기존과 동일.

## 안 할 것

- native SQL 사용
- JSONB 집계를 DB에서 처리
- 응답에 stock_code 추가
- stock_master 캐싱
- HeadlineController 변경
