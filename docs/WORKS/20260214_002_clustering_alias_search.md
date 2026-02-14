# 클러스터링에서 stock_alias 참조

## 목적

클러스터링 시 keyword 매칭이 news_extraction.keywords와 exact match로만 동작한다.
LLM이 "삼전", "삼성" 등 별칭으로 추출한 경우 "삼성전자"로 클러스터링 요청해도 해당 뉴스가 누락된다.

stock_alias 테이블을 참조하여, 종목명 + 별칭 전체로 검색 범위를 확장한다.

## 결정사항

- 클러스터링 keyword는 항상 종목명이 들어온다고 가정 (향후 스케줄러가 종목별로 호출)
- keyword로 stock_alias에서 해당 종목의 alias 목록을 조회
- 종목명 자체 + alias들을 모두 OR 조건으로 keywords 검색
- extraction 단계는 건드리지 않음 (RAG 없이는 정규화 어려움)

## 안 할 것

- extraction 파이프라인 변경
- keyword가 종목명이 아닌 경우의 분기 처리
- stock_alias 테이블 생성 (별도 작업으로 진행 중)
