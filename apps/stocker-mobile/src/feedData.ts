// ─── Stock Types ───

export interface Stock {
  code: string;
  name: string;
  sector: string;
  price: string;
  change: number;
}

export interface TypeStyle {
  color: string;
  bg: string;
  icon: string;
}

// ─── Feed Item Types (Discriminated Union) ───

export interface TimeInfo {
  display: string;
  session: "pre" | "open" | "post";
  firstReported?: string;
}

export interface StockReaction {
  code: string;
  reaction: number;
  role: string;
}

export interface StockChange {
  code: string;
  change: number;
}

export interface HighlightCommentData {
  user: string;
  time: string;
  text: string;
  likes: number;
}

export interface PollOption {
  label: string;
  emoji: string;
  votes: number;
}

export interface PollData {
  question: string;
  options: PollOption[];
}

export interface ReactionData {
  emoji: string;
  label: string;
  count: number;
}

export interface CommunityData {
  type: "comments_and_poll" | "reactions" | "comments_only" | "poll_only";
  totalComments: number;
  poll?: PollData;
  reactions?: ReactionData[];
  highlightComments?: HighlightCommentData[];
}

export interface SessionDividerItem {
  type: "session_divider";
  id: string;
  session: "pre" | "open" | "post" | "yesterday";
  label: string;
  timeRange: string;
}

export interface EventItem {
  type: "event";
  id: string;
  headline: string;
  summary: string;
  eventType: string;
  importance: "high" | "medium" | "low";
  hasImage?: boolean;
  time: TimeInfo;
  stocks: StockReaction[];
  relatedStocks: StockReaction[];
  articleCount: number;
  community: CommunityData;
}

export interface SectorMoodItem {
  type: "sector_mood";
  id: string;
  sector: string;
  emoji: string;
  mood: "hot" | "cool";
  summary: string;
  stocks: StockChange[];
  detail: string;
  time: TimeInfo;
}

export interface HistoryItem {
  type: "history";
  id: string;
  stockCode: string;
  title: string;
  current: { label: string; reaction: number; date: string };
  past: { label: string; reaction: number; date: string };
  insight: string;
}

export interface AnomalyItem {
  type: "anomaly";
  id: string;
  stockCode: string;
  emoji: string;
  title: string;
  stat: string;
  detail: string;
  trend: number[];
}

export interface ConnectionItem {
  type: "connection";
  id: string;
  title: string;
  pair: [string, string];
  count: number;
  period: string;
  topKeywords: string[];
}

export interface StatItem {
  type: "stat";
  id: string;
  emoji: string;
  title: string;
  items: { code: string; newsCount: number }[];
}

export type FeedItem =
  | SessionDividerItem
  | EventItem
  | SectorMoodItem
  | HistoryItem
  | AnomalyItem
  | ConnectionItem
  | StatItem;

// ─── Data ───

export const STOCKS: Record<string, Stock> = {
  "005930": { code: "005930", name: "삼성전자", sector: "반도체", price: "72,400", change: +2.3 },
  "000660": { code: "000660", name: "SK하이닉스", sector: "반도체", price: "185,200", change: +3.1 },
  "035420": { code: "035420", name: "NAVER", sector: "인터넷", price: "214,500", change: +0.8 },
  "051910": { code: "051910", name: "LG화학", sector: "화학", price: "387,000", change: -2.5 },
  "006400": { code: "006400", name: "삼성SDI", sector: "2차전지", price: "412,000", change: +1.7 },
};

export const TYPE_STYLES: Record<string, TypeStyle> = {
  "실적": { color: "#92400E", bg: "#FEF3C7", icon: "📊" },
  "사업": { color: "#1E40AF", bg: "#DBEAFE", icon: "🚀" },
  "산업": { color: "#3730A3", bg: "#E0E7FF", icon: "🏭" },
  "구조개편": { color: "#991B1B", bg: "#FEE2E2", icon: "🔄" },
  "기술": { color: "#065F46", bg: "#D1FAE5", icon: "⚡" },
};

export const FEED: FeedItem[] = [
  // ─── 장후 (15:30~) ───
  { type: "session_divider", id: "div_post", session: "post", label: "장 마감 후", timeRange: "15:30 ~" },

  {
    type: "sector_mood",
    id: "s1",
    sector: "반도체",
    emoji: "🔥",
    mood: "hot",
    summary: "오늘 뉴스 23건 · 평균 +2.1%",
    stocks: [
      { code: "005930", change: +2.3 },
      { code: "000660", change: +3.1 },
    ],
    detail: "HBM 수요 급증 이슈로 반도체 섹터 전반 강세",
    time: { display: "15:42", session: "post" },
  },

  {
    type: "event",
    id: "e1",
    headline: "삼성전자 2분기 실적, 시장 기대치 상회",
    summary: "영업이익 10.4조원으로 전년 대비 15배 증가. 메모리 반도체 가격 상승이 주요 원인.",
    eventType: "실적",
    importance: "high",
    hasImage: true,
    time: { display: "16:02", session: "post", firstReported: "16:02" },
    stocks: [{ code: "005930", reaction: +2.3, role: "주체" }],
    relatedStocks: [{ code: "000660", reaction: +1.2, role: "동종업계" }],
    articleCount: 47,
    community: {
      type: "comments_and_poll",
      totalComments: 89,
      poll: {
        question: "3분기 실적도 서프라이즈?",
        options: [
          { label: "그렇다", emoji: "🔥", votes: 342 },
          { label: "아니다", emoji: "🤔", votes: 156 },
        ],
      },
      highlightComments: [
        { user: "반도체덕후", time: "16:15", text: "HBM 비중이 계속 늘어나는 게 핵심. 파운드리는 아직 적자인데 거기서 얼마나 줄이냐가 관건", likes: 47 },
        { user: "가치투자er", time: "16:28", text: "실적 좋은데 주가 반응이 +2%면 이미 선반영된 거 아닌가", likes: 31 },
      ],
    },
  },

  // ─── 장중 (09:00~15:30) ───
  { type: "session_divider", id: "div_open", session: "open", label: "장중", timeRange: "09:00 ~ 15:30" },

  {
    type: "event",
    id: "e2",
    headline: "SK하이닉스, HBM4 양산 일정 6개월 앞당겨",
    summary: "엔비디아 차세대 GPU용 HBM4를 하반기 양산 개시. 월 생산 캐파 2만장 목표.",
    eventType: "사업",
    importance: "high",
    hasImage: true,
    time: { display: "13:18", session: "open", firstReported: "13:18" },
    stocks: [{ code: "000660", reaction: +3.1, role: "주체" }],
    relatedStocks: [{ code: "005930", reaction: +0.5, role: "경쟁사" }],
    articleCount: 23,
    community: {
      type: "reactions",
      totalComments: 34,
      reactions: [
        { emoji: "🚀", label: "호재", count: 287 },
        { emoji: "🤷", label: "글쎄", count: 43 },
        { emoji: "😰", label: "이미 반영", count: 91 },
      ],
      highlightComments: [
        { user: "HBM전문가", time: "13:45", text: "HBM4는 기술 난이도가 다른 레벨. 경쟁사 대비 6개월 리드는 확실히 의미 있음", likes: 62 },
      ],
    },
  },

  {
    type: "history",
    id: "h1",
    stockCode: "005930",
    title: "지난 실적 발표와 비교하면",
    current: { label: "이번 (2분기)", reaction: +2.3, date: "오늘" },
    past: { label: "지난 (1분기)", reaction: +5.2, date: "3개월 전" },
    insight: "1분기 때보다 반응이 절반. 이미 실적 기대가 주가에 반영된 것으로 보임.",
  },

  {
    type: "event",
    id: "e3",
    headline: "NAVER 하이퍼클로바X, 기업용 서비스 정식 출시",
    summary: "B2B AI 서비스 본격 상용화. 삼성SDS, 현대차 등 10개 대기업과 계약 체결.",
    eventType: "사업",
    importance: "medium",
    time: { display: "10:45", session: "open", firstReported: "10:45" },
    stocks: [{ code: "035420", reaction: +0.8, role: "주체" }],
    relatedStocks: [],
    articleCount: 12,
    community: {
      type: "comments_only",
      totalComments: 8,
      highlightComments: [
        { user: "AI투자자", time: "11:02", text: "GPT-4o 대비 한국어 성능은 확실히 좋은데, 기업들이 실제로 돈을 낼지가 관건", likes: 18 },
      ],
    },
  },

  {
    type: "anomaly",
    id: "a1",
    stockCode: "051910",
    emoji: "📈",
    title: "LG화학 뉴스량 급증",
    stat: "5일 평균 대비 3.2배",
    detail: "구조개편 관련 보도가 집중되고 있음",
    trend: [1, 1, 2, 1, 2, 3, 8, 12, 15],
  },

  // ─── 장전 (~09:00) ───
  { type: "session_divider", id: "div_pre", session: "pre", label: "장 시작 전", timeRange: "~ 09:00" },

  {
    type: "event",
    id: "e4",
    headline: "LG화학, 배터리 소재 사업부 분할 검토",
    summary: "양극재·분리막 사업을 별도 법인으로 분리하는 방안 이사회에서 논의 예정.",
    eventType: "구조개편",
    importance: "medium",
    time: { display: "07:32", session: "pre", firstReported: "07:32" },
    stocks: [{ code: "051910", reaction: -2.5, role: "주체" }],
    relatedStocks: [{ code: "006400", reaction: +0.3, role: "수혜 가능" }],
    articleCount: 15,
    community: {
      type: "poll_only",
      totalComments: 21,
      poll: {
        question: "분할, 주주에게 호재일까?",
        options: [
          { label: "호재", emoji: "👍", votes: 89 },
          { label: "악재", emoji: "👎", votes: 234 },
          { label: "모르겠다", emoji: "🤷", votes: 67 },
        ],
      },
    },
  },

  {
    type: "connection",
    id: "c1",
    title: "자주 같이 등장하는 종목",
    pair: ["005930", "000660"],
    count: 15,
    period: "최근 30일",
    topKeywords: ["HBM", "메모리", "반도체 업황", "엔비디아"],
  },

  {
    type: "event",
    id: "e5",
    headline: "2차전지 3사, 유럽 공장 증설 경쟁 본격화",
    summary: "LG·삼성SDI·SK온이 동시에 유럽 생산능력 확대. EU 보조금 확보 경쟁 가열.",
    eventType: "산업",
    importance: "low",
    time: { display: "06:15", session: "pre", firstReported: "06:15" },
    stocks: [
      { code: "051910", reaction: -0.5, role: "당사자" },
      { code: "006400", reaction: +1.7, role: "당사자" },
    ],
    relatedStocks: [],
    articleCount: 8,
    community: {
      type: "reactions",
      totalComments: 5,
      reactions: [
        { emoji: "🏭", label: "기대", count: 34 },
        { emoji: "💸", label: "돈이 너무 많이", count: 56 },
      ],
    },
  },

  // ─── 어제 장후 ───
  { type: "session_divider", id: "div_yesterday_post", session: "yesterday", label: "어제 장 마감 후", timeRange: "2.3(월)" },

  {
    type: "stat",
    id: "st1",
    emoji: "🏆",
    title: "이번 주 가장 뜨거운 종목",
    items: [
      { code: "000660", newsCount: 52 },
      { code: "005930", newsCount: 48 },
      { code: "051910", newsCount: 31 },
    ],
  },
];
