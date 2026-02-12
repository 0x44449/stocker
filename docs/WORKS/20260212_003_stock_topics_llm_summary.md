# stock-topics API에 LLM 요약 제목 추가

## 배경

클러스터링으로 비슷한 뉴스를 묶을 수 있게 됐다.
가장 큰 클러스터에 대해 LLM으로 대표 제목을 생성하여 topic으로 반환한다.

## 목표

stock-topics API 응답에 LLM 요약 제목 추가.

## 관련 파일

- `apps/news-analyzer/clustering/service.py`
- `apps/news-analyzer/clustering/router.py`

## 할 것

### 1. LLM 요약 함수 추가

- 클러스터에 포함된 기사 제목들을 Ollama(qwen2.5:7b)에게 전달
- "이 기사들의 공통 주제를 한 줄로 요약해줘" 프롬프트
- 대표 제목 1줄 반환

### 2. 응답 구조 변경

기존 clusters 구조에서 topic/clusters/noise 분리 구조로 변경.

```json
{
  "keyword": "삼성전자",
  "total_count": 25,
  "topic": {
    "title": "삼성전자 2분기 실적, 시장 기대치 상회",
    "count": 8,
    "articles": [
      { "news_id": 123, "title": "삼성전자 2분기 영업이익 10.4조원 달성" },
      { "news_id": 456, "title": "삼성전자 실적 시장 기대치 상회" }
    ]
  },
  "clusters": [
    {
      "count": 3,
      "articles": [...]
    }
  ],
  "noise": [
    { "news_id": 789, "title": "..." }
  ]
}
```

- topic: 가장 큰 클러스터 + LLM 생성 제목. 클러스터가 없으면 null
- clusters: topic을 제외한 나머지 클러스터 (count 내림차순)
- noise: 어디에도 묶이지 않은 기사

### 3. LLM 호출

- 기존 extraction/service.py의 Ollama 호출 방식과 동일하게 사용
- 모델: qwen2.5:7b (하드코딩)
- topic 클러스터의 기사 제목만 전달 (본문 아님)
- 프롬프트: 뉴스 헤드라인 스타일로 작성하도록 유도
```
아래 뉴스 제목들을 대표하는 뉴스 헤드라인을 하나 만들어줘.
실제 뉴스 기사 제목처럼 작성해. 다른 설명 없이 헤드라인만 출력해.
```

## 안 할 것

- 모든 클러스터에 LLM 요약 적용 (topic만)
- 본문 포함한 요약
- 결과 DB 저장
- 스케줄러 배치
- Spring Boot 쪽 변경 (pass-through이므로 자동 반영)
