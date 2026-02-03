# 뉴스 매핑 목록 조회 API extraction 필터 추가

## 배경

뉴스 매핑 목록 조회 시, extraction(LLM 기업명 추출) 작업이 된 뉴스만 보거나 안 된 뉴스만 보고 싶은 경우가 있다.
기존 filter(all/reviewed/unreviewed)와 독립적으로 조합 가능한 별도 파라미터로 추가한다.

## 목표

- 목록 조회 API에 `extraction` 파라미터 추가

## 관련 테이블

```sql
-- news_company_extraction (V6)
-- extraction row 존재 여부로 판단 (status 값과 무관)
id, news_id (UNIQUE), status, created_at, processed_at
```

## 할 것

### API 변경

```
GET /api/news-mappings?filter={filter}&extraction={extraction}&page={page}&size={size}&search={search}
```

신규 파라미터:
- `extraction`: `all` (상관없음, 기본값), `extracted` (extraction row 있음), `unextracted` (extraction row 없음)

기존 `filter`(all/reviewed/unreviewed)와 독립적으로 AND 조합.

### 구현 범위

- `NewsMappingController`: `extraction` 파라미터 추가
- `NewsMappingService.getList()`: extraction 필터 조건 추가 (서브쿼리로 news_company_extraction 존재 여부 체크)

## 안 할 것

- extraction status 값에 따른 필터링 (row 존재 여부만 본다)
- 다른 API 변경
- 프론트엔드 수정

## 완료 조건

- [ ] extraction 파라미터 추가 (기본값 all)
- [ ] extracted 필터: extraction row 있는 뉴스만
- [ ] unextracted 필터: extraction row 없는 뉴스만
- [ ] 기존 filter와 독립적으로 조합 동작
