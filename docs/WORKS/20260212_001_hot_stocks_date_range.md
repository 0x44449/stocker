# 핫 종목 API 날짜 범위 확장

## 배경

현재 HotStockService가 오늘(today) 하루만 조회한다.
데이터가 적을 수 있으므로 어제+오늘 2일 범위로 확장한다.

## 목표

HotStockService의 published_at 필터를 어제~오늘로 변경한다.

## 관련 파일

- `apps/api/src/main/java/com/hanzi/stocker/api/feed/HotStockService.java`

## 할 것

- `startOfDay`를 `today.minusDays(1).atStartOfDay()`로 변경

## 안 할 것

- 파라미터화
- HeadlineService 수정
