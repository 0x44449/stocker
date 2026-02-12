# 크롤링/분석 스케줄 조정

## 목표

뉴스 크롤링, extraction, embedding 스케줄을 조정하여 데이터 갱신 빈도를 높인다.

## 할 것

### 1. 뉴스 크롤링 스케줄 변경 (Spring)

파일: `apps/api/src/main/java/com/hanzi/stocker/ingest/news/NewsCrawlScheduler.java`

- 변경 전: `0 0 9,12,15,18 * * *` (9,12,15,18시)
- 변경 후: `0 0 7,9,12,15,18 * * *` (7,9,12,15,18시)
- 7시 추가

### 2. extraction 스케줄 변경 (Python)

파일: `apps/news-analyzer/main.py`

- 변경 전: `hour="9,21"`
- 변경 후: `hour="8,10,13,16,19"`

### 3. embedding 스케줄 변경 (Python)

파일: `apps/news-analyzer/main.py`

- 변경 전: `hour="0,12"`
- 변경 후: `hour="9,11,14,17,20"`

## 안 할 것

- job 로직 변경 없음
- 그 외 코드 변경 없음
