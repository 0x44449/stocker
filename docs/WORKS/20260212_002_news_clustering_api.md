# 뉴스 클러스터링 API (Python Analyzer)

## 배경

같은 종목 관련 뉴스 중 비슷한 주제끼리 묶어서 보여주고 싶다.
news_embedding의 벡터 유사도 기반으로 DBSCAN 클러스터링을 수행하는 API를 Python analyzer에 만든다.

## 목표

Python analyzer에 클러스터링 API 추가.

## 관련 테이블

```
news_extraction   - extraction_id, news_id, keywords (JSONB), llm_model, prompt_version, published_at
news_embedding    - id, news_id, embedding (vector(1024)), created_at
news_raw          - id, title, published_at
```

## 할 것

### 1. 의존성 추가

- `scikit-learn` 설치 (DBSCAN용)

### 2. 클러스터링 API

- `POST /clustering/similar-news`

요청:
```json
{
  "keyword": "삼성전자",
  "days": 2,
  "eps": 0.2
}
```
- keyword: 종목명 (news_extraction.keywords에서 필터)
- days: 오늘 기준 며칠 범위 (기본값 2)
- eps: DBSCAN eps 파라미터 (기본값 0.2, 코사인 거리 기준. 1 - similarity이므로 eps=0.2는 유사도 0.8 이상)

내부 로직:
1. news_extraction에서 해당 keyword + published_at 날짜 범위 + llm_model/prompt_version(하드코딩) 조건으로 news_id 조회
2. news_embedding에서 해당 news_id들의 embedding 로드
3. news_raw에서 해당 news_id들의 title 로드
4. DBSCAN(eps, min_samples=2, metric='cosine')으로 클러스터링
5. 클러스터별 기사 목록 구성

응답:
```json
{
  "keyword": "삼성전자",
  "total_count": 25,
  "clusters": [
    {
      "cluster_id": 0,
      "count": 5,
      "articles": [
        { "news_id": 123, "title": "삼성전자 2분기 실적..." },
        { "news_id": 456, "title": "삼성전자 영업이익..." }
      ]
    },
    {
      "cluster_id": 1,
      "count": 3,
      "articles": [...]
    }
  ],
  "noise": [...]
}
```
- clusters: count 내림차순 정렬
- noise: 어디에도 묶이지 않은 기사 (DBSCAN label=-1)

### 3. 파일 구조

- `apps/news-analyzer/clustering/` 패키지 생성
- `router.py`: API 엔드포인트
- `service.py`: 클러스터링 로직

### 4. FastAPI 라우터 등록

- main.py에 clustering router 추가

### 5. Spring Boot 연동

- `GET /api/feed/stock-topics?keyword=삼성전자&days=2&eps=0.2`
- `api/feed/` 패키지에 추가
- Python analyzer의 `POST /clustering/similar-news`를 호출하여 결과 그대로 반환 (pass-through)
- RestTemplate 또는 WebClient로 Python API 호출

### 6. Dockerfile 수정

- `infra/docker/dockerfiles/news-analyzer.Dockerfile`에 `COPY clustering clustering` 추가 (`COPY search search` 다음 줄)

### 7. 설정 추가

- `application.yml`에 analyzer URL 설정 추가 (예: `analyzer.url=http://localhost:8000`)
- docker-compose.yml의 api 서비스에 환경변수 추가: `ANALYZER_URL: http://news-analyzer:8000`

### 8. numpy 직렬화 버그 수정

- `clustering/service.py`에서 DBSCAN이 반환하는 numpy.int64를 Python int()로 변환
- cluster_id, news_id, count 등 응답에 포함되는 숫자값 대상

### 9. 클러스터링 날짜 기준 수정

- `clustering/service.py`에서 `datetime.now() - timedelta(days=days)` 대신 오늘 날짜 기준 `LocalDate` 방식으로 변경
- days=2면 오늘+어제 (오늘 0시 ~ 내일 0시가 아닌, 어제 0시 ~ 내일 0시)

## 안 할 것

- 클러스터 대표 주제 생성 (LLM 요약)
- 클러스터링 결과 DB 저장
