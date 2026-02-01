import { NewsMappingSummary, NewsMappingDetail, Stock } from "./types";

// Phase 1에서는 API 없이 mock 데이터로 동작. Phase 2에서 실제 API로 교체.

export const mockMappingList: NewsMappingSummary[] = [
  {
    newsId: 1,
    title: "현대차그룹, 전기차 배터리 투자 확대 계획 발표",
    extractedNames: ["현대차그룹", "LG에너지솔루션", "삼성SDI"],
    matchedCount: 1,
    totalCount: 3,
  },
  {
    newsId: 2,
    title: "삼성전자, 2분기 실적 시장 예상 상회",
    extractedNames: ["삼성전자"],
    matchedCount: 1,
    totalCount: 1,
  },
  {
    newsId: 3,
    title: "애플·구글, AI 경쟁 가속화...국내 반도체 수혜 전망",
    extractedNames: ["애플", "구글", "SK하이닉스"],
    matchedCount: 1,
    totalCount: 3,
  },
  {
    newsId: 4,
    title: "카카오, 신사업 진출로 주가 급등",
    extractedNames: ["카카오"],
    matchedCount: 1,
    totalCount: 1,
  },
  {
    newsId: 5,
    title: "네이버·카카오 클라우드 사업 경쟁 심화",
    extractedNames: ["네이버", "카카오"],
    matchedCount: 2,
    totalCount: 2,
  },
];

export const mockMappingDetails: Record<number, NewsMappingDetail> = {
  1: {
    newsId: 1,
    title: "현대차그룹, 전기차 배터리 투자 확대 계획 발표",
    mappings: [
      {
        id: 101,
        extractedName: "현대차그룹",
        matchedStockCode: null,
        matchType: "none",
        verified: false,
        feedback: null,
      },
      {
        id: 102,
        extractedName: "LG에너지솔루션",
        matchedStockCode: "373220",
        matchType: "auto_exact",
        verified: false,
        feedback: null,
      },
      {
        id: 103,
        extractedName: "삼성SDI",
        matchedStockCode: null,
        matchType: "none",
        verified: false,
        feedback: null,
      },
    ],
  },
  2: {
    newsId: 2,
    title: "삼성전자, 2분기 실적 시장 예상 상회",
    mappings: [
      {
        id: 201,
        extractedName: "삼성전자",
        matchedStockCode: "005930",
        matchType: "auto_exact",
        verified: false,
        feedback: null,
      },
    ],
  },
  3: {
    newsId: 3,
    title: "애플·구글, AI 경쟁 가속화...국내 반도체 수혜 전망",
    mappings: [
      {
        id: 301,
        extractedName: "애플",
        matchedStockCode: null,
        matchType: "none",
        verified: false,
        feedback: null,
      },
      {
        id: 302,
        extractedName: "구글",
        matchedStockCode: null,
        matchType: "none",
        verified: false,
        feedback: null,
      },
      {
        id: 303,
        extractedName: "SK하이닉스",
        matchedStockCode: "000660",
        matchType: "auto_exact",
        verified: false,
        feedback: null,
      },
    ],
  },
};

export const mockStocks: Stock[] = [
  { stockCode: "005930", nameKr: "삼성전자", nameKrShort: "삼성전자" },
  { stockCode: "000660", nameKr: "SK하이닉스", nameKrShort: "SK하이닉스" },
  { stockCode: "005380", nameKr: "현대자동차", nameKrShort: "현대차" },
  { stockCode: "012330", nameKr: "현대모비스", nameKrShort: "현대모비스" },
  { stockCode: "086280", nameKr: "현대글로비스", nameKrShort: "현대글로비스" },
  { stockCode: "373220", nameKr: "LG에너지솔루션", nameKrShort: "LG에너지솔루션" },
  { stockCode: "006400", nameKr: "삼성SDI", nameKrShort: "삼성SDI" },
  { stockCode: "035420", nameKr: "NAVER", nameKrShort: "NAVER" },
  { stockCode: "035720", nameKr: "카카오", nameKrShort: "카카오" },
  { stockCode: "051910", nameKr: "LG화학", nameKrShort: "LG화학" },
];
