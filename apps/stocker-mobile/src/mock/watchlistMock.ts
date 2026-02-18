// 관심종목 Mock 데이터

export interface ArticleItem {
  id: string;
  title: string;
  source: string;
  time: string;
  url: string;
}

export interface ClusterItem {
  clusterId: string;
  category: string;
  headline: string;
  summary: string;
  time: string;
  session: "pre" | "open" | "post";
  articleCount: number;
  changeRate: number;
  relatedStocks: { name: string; role: string; changeRate: number }[];
  articles: ArticleItem[];
}

export interface WatchlistStock {
  stockCode: string;
  stockName: string;
  price: number;
  changeRate: number;
  changeAmount: number;
  newsCount: number;
  clusters: ClusterItem[];
}

export const WATCHLIST_STOCKS: WatchlistStock[] = [
  {
    stockCode: "005930",
    stockName: "삼성전자",
    price: 72400,
    changeRate: 2.3,
    changeAmount: 1600,
    newsCount: 12,
    clusters: [
      {
        clusterId: "c-sam-1",
        category: "실적",
        headline: "삼성전자 2분기 실적, 시장 기대치 상회",
        summary: "영업이익 10.4조원으로 전년 대비 15배 증가. 메모리 반도체 가격 상승이 주요 원인으로 분석됨.",
        time: "15:42",
        session: "post",
        articleCount: 5,
        changeRate: 2.3,
        relatedStocks: [
          { name: "SK하이닉스", role: "동종업계", changeRate: 3.1 },
          { name: "삼성SDI", role: "자회사", changeRate: 1.2 },
        ],
        articles: [
          { id: "a1", title: "삼성전자, 2분기 영업이익 10.4조…HBM 효과 톡톡", source: "한국경제", time: "15:30", url: "https://example.com/1" },
          { id: "a2", title: "삼성전자 실적 서프라이즈에 반도체 ETF 동반 상승", source: "매일경제", time: "15:15", url: "https://example.com/2" },
          { id: "a3", title: "삼성전자 HBM3E 양산 본격화…엔비디아 납품 확대", source: "이투데이", time: "14:50", url: "https://example.com/3" },
          { id: "a4", title: "반도체 업황 회복에 삼성전자 목표가 상향 잇따라", source: "서울경제", time: "14:20", url: "https://example.com/4" },
          { id: "a5", title: "삼성전자, AI 반도체 수혜 본격화 전망", source: "한국경제", time: "13:45", url: "https://example.com/5" },
        ],
      },
      {
        clusterId: "c-sam-2",
        category: "사업",
        headline: "삼성 파운드리, 2nm GAA 공정 수율 개선",
        summary: "삼성전자가 2nm 게이트올어라운드(GAA) 공정 수율을 60%대까지 끌어올렸다는 소식에 파운드리 사업 기대감 확대.",
        time: "11:20",
        session: "open",
        articleCount: 3,
        changeRate: 1.5,
        relatedStocks: [
          { name: "TSMC", role: "경쟁사", changeRate: -0.4 },
        ],
        articles: [
          { id: "a6", title: "삼성 파운드리, 2nm GAA 수율 60%대 돌파", source: "매일경제", time: "11:10", url: "https://example.com/6" },
          { id: "a7", title: "삼성전자 파운드리, TSMC와 격차 좁히나", source: "한국경제", time: "10:45", url: "https://example.com/7" },
          { id: "a8", title: "삼성 2nm 공정 진전에 퀄컴 주문 기대", source: "이투데이", time: "10:20", url: "https://example.com/8" },
        ],
      },
      {
        clusterId: "c-sam-3",
        category: "경영",
        headline: "이재용 회장, AI 반도체 투자 10조 추가 집행 발표",
        summary: "삼성전자가 AI 반도체 분야에 향후 3년간 10조원 추가 투자를 발표. 미국 텍사스 공장 증설 포함.",
        time: "08:30",
        session: "pre",
        articleCount: 4,
        changeRate: 0.8,
        relatedStocks: [
          { name: "삼성SDI", role: "계열사", changeRate: 0.5 },
          { name: "삼성전기", role: "계열사", changeRate: 0.3 },
        ],
        articles: [
          { id: "a9", title: "이재용 회장 'AI 반도체에 10조 추가 투자'", source: "서울경제", time: "08:20", url: "https://example.com/9" },
          { id: "a10", title: "삼성전자 텍사스 공장 증설 계획 공식화", source: "한국경제", time: "08:10", url: "https://example.com/10" },
          { id: "a11", title: "삼성, AI 반도체 투자 확대로 엔비디아 추격", source: "매일경제", time: "07:50", url: "https://example.com/11" },
          { id: "a12", title: "삼성전자 AI 투자 발표에 반도체 관련주 동반 강세", source: "이투데이", time: "07:30", url: "https://example.com/12" },
        ],
      },
    ],
  },
  {
    stockCode: "000660",
    stockName: "SK하이닉스",
    price: 185200,
    changeRate: 3.1,
    changeAmount: 5600,
    newsCount: 8,
    clusters: [
      {
        clusterId: "c-skh-1",
        category: "사업",
        headline: "SK하이닉스, HBM4 샘플 엔비디아 납품 완료",
        summary: "차세대 고대역폭메모리(HBM4) 샘플을 엔비디아에 납품 완료. 내년 상반기 양산 목표.",
        time: "14:30",
        session: "open",
        articleCount: 4,
        changeRate: 3.1,
        relatedStocks: [
          { name: "삼성전자", role: "경쟁사", changeRate: 2.3 },
          { name: "한미반도체", role: "장비업체", changeRate: 5.2 },
        ],
        articles: [
          { id: "a13", title: "SK하이닉스, HBM4 샘플 엔비디아 납품 완료", source: "한국경제", time: "14:20", url: "https://example.com/13" },
          { id: "a14", title: "HBM4 시대 개막…SK하이닉스 선점 효과 기대", source: "매일경제", time: "14:00", url: "https://example.com/14" },
          { id: "a15", title: "SK하이닉스 HBM 독주에 삼성전자 추격 속도", source: "서울경제", time: "13:30", url: "https://example.com/15" },
          { id: "a16", title: "엔비디아 차세대 GPU에 SK하이닉스 HBM4 탑재 유력", source: "이투데이", time: "13:10", url: "https://example.com/16" },
        ],
      },
      {
        clusterId: "c-skh-2",
        category: "실적",
        headline: "SK하이닉스, 분기 영업이익 사상 최대 전망",
        summary: "증권가, 3분기 영업이익 7조원대 전망. HBM과 서버용 DDR5 수요 확대가 견인.",
        time: "09:15",
        session: "open",
        articleCount: 3,
        changeRate: 2.0,
        relatedStocks: [
          { name: "삼성전자", role: "동종업계", changeRate: 2.3 },
        ],
        articles: [
          { id: "a17", title: "SK하이닉스 3분기 영업이익 7조 전망…사상 최대", source: "한국경제", time: "09:10", url: "https://example.com/17" },
          { id: "a18", title: "증권가, SK하이닉스 목표가 잇따라 상향", source: "매일경제", time: "09:00", url: "https://example.com/18" },
          { id: "a19", title: "메모리 호황에 SK하이닉스 실적 서프라이즈 기대", source: "서울경제", time: "08:45", url: "https://example.com/19" },
        ],
      },
    ],
  },
  {
    stockCode: "035420",
    stockName: "NAVER",
    price: 214500,
    changeRate: -1.2,
    changeAmount: -2600,
    newsCount: 6,
    clusters: [
      {
        clusterId: "c-nav-1",
        category: "사업",
        headline: "네이버, 하이퍼클로바X 기업용 서비스 출시",
        summary: "네이버가 자체 개발 AI 모델 '하이퍼클로바X'를 기반으로 기업용 AI 서비스를 공식 출시. B2B 시장 공략 본격화.",
        time: "13:00",
        session: "open",
        articleCount: 4,
        changeRate: 0.5,
        relatedStocks: [
          { name: "카카오", role: "경쟁사", changeRate: -0.8 },
          { name: "네이버웹툰", role: "자회사", changeRate: 1.1 },
        ],
        articles: [
          { id: "a20", title: "네이버, 하이퍼클로바X 기업용 서비스 정식 출시", source: "한국경제", time: "12:50", url: "https://example.com/20" },
          { id: "a21", title: "네이버 AI 서비스 출시에 B2B 시장 경쟁 격화", source: "매일경제", time: "12:30", url: "https://example.com/21" },
          { id: "a22", title: "하이퍼클로바X, 한국어 특화 성능으로 차별화", source: "이투데이", time: "12:10", url: "https://example.com/22" },
          { id: "a23", title: "네이버 AI 사업, 수익화 시점 빨라질 듯", source: "서울경제", time: "11:50", url: "https://example.com/23" },
        ],
      },
      {
        clusterId: "c-nav-2",
        category: "규제",
        headline: "공정위, 네이버 검색 알고리즘 자사 우대 조사 착수",
        summary: "공정거래위원회가 네이버의 검색 알고리즘이 자사 서비스를 우대하는지 조사에 착수. 과징금 가능성 제기.",
        time: "10:00",
        session: "open",
        articleCount: 3,
        changeRate: -1.8,
        relatedStocks: [
          { name: "카카오", role: "동종업계", changeRate: -0.8 },
        ],
        articles: [
          { id: "a24", title: "공정위, 네이버 검색 알고리즘 자사 우대 조사", source: "한국경제", time: "09:50", url: "https://example.com/24" },
          { id: "a25", title: "네이버, 공정위 조사에 '투명하게 협조' 입장", source: "매일경제", time: "09:30", url: "https://example.com/25" },
          { id: "a26", title: "네이버 규제 리스크에 주가 하락", source: "서울경제", time: "09:15", url: "https://example.com/26" },
        ],
      },
    ],
  },
  {
    stockCode: "035720",
    stockName: "카카오",
    price: 42150,
    changeRate: -2.5,
    changeAmount: -1100,
    newsCount: 5,
    clusters: [
      {
        clusterId: "c-kko-1",
        category: "규제",
        headline: "카카오 SM 인수 관련 경영진 재판 일정 확정",
        summary: "카카오의 SM엔터테인먼트 인수 과정에서의 시세조종 혐의 재판이 다음 달 시작. 경영 불확실성 지속.",
        time: "16:00",
        session: "post",
        articleCount: 3,
        changeRate: -2.5,
        relatedStocks: [
          { name: "SM엔터", role: "피인수사", changeRate: -1.2 },
          { name: "카카오뱅크", role: "자회사", changeRate: -0.7 },
        ],
        articles: [
          { id: "a27", title: "카카오 SM 인수 관련 재판 다음달 시작", source: "한국경제", time: "15:50", url: "https://example.com/27" },
          { id: "a28", title: "카카오 경영 리스크 지속…주가 약세", source: "매일경제", time: "15:30", url: "https://example.com/28" },
          { id: "a29", title: "카카오 재판 일정에 투자심리 위축", source: "이투데이", time: "15:10", url: "https://example.com/29" },
        ],
      },
      {
        clusterId: "c-kko-2",
        category: "사업",
        headline: "카카오톡 AI 챗봇 '카나나' 베타 오픈",
        summary: "카카오가 자체 AI 챗봇 '카나나'의 베타 서비스를 시작. 카카오톡 내 자연어 대화 기반 검색/추천 서비스 제공.",
        time: "11:00",
        session: "open",
        articleCount: 3,
        changeRate: 0.3,
        relatedStocks: [
          { name: "NAVER", role: "경쟁사", changeRate: -1.2 },
        ],
        articles: [
          { id: "a30", title: "카카오, AI 챗봇 '카나나' 베타 서비스 시작", source: "서울경제", time: "10:50", url: "https://example.com/30" },
          { id: "a31", title: "카카오톡 AI 챗봇에 이용자 반응 뜨거워", source: "한국경제", time: "10:30", url: "https://example.com/31" },
          { id: "a32", title: "카카오 vs 네이버, AI 챗봇 전쟁 시작", source: "매일경제", time: "10:10", url: "https://example.com/32" },
        ],
      },
    ],
  },
  {
    stockCode: "005380",
    stockName: "현대차",
    price: 238000,
    changeRate: 0.5,
    changeAmount: 1200,
    newsCount: 0,
    clusters: [],
  },
];

// 관심종목 설정용 전체 종목 리스트 (검색용)
export const ALL_STOCKS = [
  { stockCode: "005930", stockName: "삼성전자" },
  { stockCode: "000660", stockName: "SK하이닉스" },
  { stockCode: "035420", stockName: "NAVER" },
  { stockCode: "035720", stockName: "카카오" },
  { stockCode: "005380", stockName: "현대차" },
  { stockCode: "373220", stockName: "LG에너지솔루션" },
  { stockCode: "207940", stockName: "삼성바이오로직스" },
  { stockCode: "068270", stockName: "셀트리온" },
  { stockCode: "105560", stockName: "KB금융" },
  { stockCode: "055550", stockName: "신한지주" },
  { stockCode: "051910", stockName: "LG화학" },
  { stockCode: "006400", stockName: "삼성SDI" },
  { stockCode: "003670", stockName: "포스코퓨처엠" },
  { stockCode: "000270", stockName: "기아" },
  { stockCode: "012330", stockName: "현대모비스" },
];

// clusterId로 클러스터 찾기 헬퍼
export function findClusterById(clusterId: string): { stock: WatchlistStock; cluster: ClusterItem } | null {
  for (const stock of WATCHLIST_STOCKS) {
    const cluster = stock.clusters.find((c) => c.clusterId === clusterId);
    if (cluster) return { stock, cluster };
  }
  return null;
}

// stockCode로 종목 찾기 헬퍼
export function findStockByCode(stockCode: string): WatchlistStock | null {
  return WATCHLIST_STOCKS.find((s) => s.stockCode === stockCode) ?? null;
}
