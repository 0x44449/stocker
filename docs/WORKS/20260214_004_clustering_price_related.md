# 클러스터링 응답에 주가 변동 및 관련 종목 추가

## 목적

클러스터링 결과에 해당 종목의 주가 변동 정보와 관련 종목을 함께 보여주고 싶다.
주가 변동은 stock_price_daily_raw에서 최근 거래일 데이터를 사용한다.
관련 종목은 별도 매핑 없이, 클러스터 내 뉴스들의 keywords에서 주체 종목 외에 다른 종목을 카운팅하여 추출한다.

## 결정사항

### 주가 변동

- 클러스터의 keyword(종목명)에 해당하는 stock_price_daily_raw에서 가장 최근 거래일의 종가/등락률 조회
- 장중 지연 데이터는 아직 없으므로, 있는 데이터 기준으로만 제공

### 관련 종목

- 클러스터 내 뉴스들의 keywords를 모아서, 주체 종목(keyword) 외에 stock_master에 매칭되는 다른 종목을 카운팅
- 빈도순으로 정렬하여 응답에 포함
- alias, subsidiary도 정규화하여 카운팅 (stock_alias, subsidiary_mapping 참조)
- 관련 종목의 주가 변동도 함께 제공

### 응답 필드

- `stock_price`: keyword 종목의 주가 정보. stock_code, date, close, diff_rate
- `related_stock`: 클러스터 내 다른 종목 중 언급 최다 1건. stock_name, stock_code, mention_count, close, diff_rate. 없으면 null

## 안 할 것

- 장중 지연 데이터 수집
- 테마/섹터 분류
- 경쟁사 관계 테이블 구축
