# 피드 카드 디자인 샘플 맞춤 개선

## 목적

피드 탭의 카드 디자인을 샘플 탭(FeedScreen.tsx)의 EventCard와 동일한 스타일로 변경한다.
API 데이터가 없는 부분은 하드코딩으로 채우고, 미구현 영역은 제외한다.

## 참고 파일

- 샘플 카드 디자인: `apps/stocker-mobile/src/FeedScreen.tsx`의 EventCard 컴포넌트
- 수정 대상: `apps/stocker-mobile/app/(tabs)/index.tsx`

## 변경사항

### 카드 구조 (EventCard와 동일하게)

1. **상단 이미지 영역**
   - 샘플의 `eventImageContainer`와 동일하게 LinearGradient로 배경 생성
   - 장식 원형(decoCircle) 포함
   - 하단 그라데이션 오버레이

2. **이미지 영역 우상단**: `기사 N건` 배지 표시
   - 샘플의 `eventArticleCount` 스타일 그대로 사용
   - 데이터: `data.total_count` 또는 `data.topic?.count` 사용

3. **이미지 영역 좌하단**: 이벤트 타입 + 세션 시간
   - 샘플의 `eventImageBadges` 스타일 그대로 사용
   - **하드코딩**: 이벤트 타입은 "실적", 세션은 "장후", 시간은 "16:02" 등 고정값
   - 나중에 데이터로 교체할 예정

4. **이미지 아래: 헤드라인 + 요약**
   - 샘플의 `eventHeadline`, `eventSummary` 스타일 그대로 사용
   - 데이터: `data.topic?.title`, `data.topic?.summary`

5. **주식 변화량 영역**
   - 샘플의 `stockReactionArea`, `stockReactionRow` 스타일 그대로 사용
   - 주체 종목: `data.keyword` + `data.stock_price` → role은 "주체"
   - 연관 종목: `data.related_stock` → role은 "연관종목" (샘플의 "동종업계" 대신)
   - 변화율 바 + 퍼센트 표시 동일

### 안 할 것

- 하단 커뮤니티 영역 (댓글, 투표, 리액션) 만들지 않음
- 카드 상세 뷰(클릭 시 상세 페이지)
- 세션 디바이더
- 이미지 영역의 실제 이미지 로딩 (그라데이션 배경만)

### 스타일 참고

샘플 FeedScreen.tsx에서 아래 스타일/컴포넌트를 그대로 가져와서 사용:
- `eventImageContainer`, `decoCircle`, `eventImageOverlay`
- `eventImageBadges`, `eventTypeBadgeOnImage`, `eventArticleCount`
- `eventHeadline`, `eventSummary`
- `stockReactionArea`, `stockReactionRow`, `stockName`, `stockRole`
- `reactionBarTrack`, `reactionBarFill`, `reactionPercent`
- `TimeDisplay` 컴포넌트 (하드코딩된 값으로)
- `LinearGradient` 임포트 필요 (`expo-linear-gradient`)

### 하드코딩 값

```typescript
// 이미지 영역 좌하단
eventType: "실적"
session: "장후"  
timeDisplay: "16:02"
```
