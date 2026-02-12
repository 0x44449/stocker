# LLM 모델을 EXAONE 3.5로 변경

## 배경

qwen2.5:7b로 뉴스 헤드라인 요약 시 중국어/영어가 섞여 나오는 문제 발생.
EXAONE 3.5는 LG AI Research의 한국어+영어 이중 언어 모델로 한국어 품질이 좋다.

## 목표

토픽 요약 + extraction(기업명 추출) 모델을 exaone3.5:7.8b로 변경.

## 관련 파일

- `apps/news-analyzer/clustering/service.py` — 토픽 요약 모델
- `apps/news-analyzer/extraction/service.py` — 기업명 추출 모델

## 할 것

### 1. 토픽 요약 모델 변경

- `clustering/service.py`의 `_summarize_topic()` 모델을 `qwen2.5:7b` → `exaone3.5:7.8b`로 변경
- `clustering/service.py`의 `LLM_MODEL` 상수는 `qwen2.5:7b` 유지 (기존 데이터 필터용)

### 2. 기업명 추출 모델 변경

- `extraction/service.py`의 LLM 모델을 `qwen2.5:7b` → `exaone3.5:7.8b`로 변경
- `extraction/job.py`의 `LLM_MODEL` 상수를 `qwen2.5:7b` → `exaone3.5:7.8b`로 변경

### 3. 사전 준비

- Ollama에 exaone3.5:7.8b 모델 pull 필요 (`ollama pull exaone3.5:7.8b`)

## 안 할 것

- HotStockService 모델 필터 변경 (기존 qwen2.5:7b 데이터 조회 필요, 새 데이터 쌓이면 별도 변경)
- clustering/service.py의 LLM_MODEL 필터 상수 변경 (위와 동일)
- 프롬프트 변경 (현재 프롬프트 유지, 결과 보고 판단)
- Spring Boot 쪽 변경
