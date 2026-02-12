# 토픽 카드에 요약 본문 추가

## 배경

토픽 카드에 제목만 있으면 밋밋하다. 클러스터 기사들의 본문을 기반으로 2~3줄 요약을 추가한다.

## 목표

토픽에 summary 필드 추가.

## 관련 파일

- `apps/news-analyzer/clustering/service.py`

## 할 것

### 1. 요약 본문 생성 함수 추가

- 클러스터 기사 중 상위 5개만 사용
- news_raw.raw_text에서 앞부분 300자씩 잘라서 LLM에 전달
- LLM(exaone3.5:7.8b)에게 2~3줄 요약 요청
- 결과를 topic.summary에 추가

### 2. 응답 구조

```json
"topic": {
  "title": "삼성전자, HBM5 자신감 폭발…업계 1위 선언",
  "summary": "삼성전자가 차세대 고대역폭 메모리 HBM5에서 1위를 차지하겠다고 선언했다. 성능을 2.8배 높인 맞춤형 HBM 신제품도 공개하며 메모리 시장 주도권 탈환에 나섰다.",
  "count": 5,
  "articles": [...]
}
```

### 3. news_raw 본문 조회

- 현재 service.py에서 news_raw.title만 조회 중
- news_raw.raw_text도 함께 조회하도록 변경 (topic 클러스터 상위 5개만)

## 안 할 것

- 전체 기사 본문 사용 (상위 5개, 300자 제한)
- Spring Boot 쪽 변경 (pass-through)
- 프롬프트 최적화 (결과 보고 판단)
