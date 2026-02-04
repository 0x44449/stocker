import { useState } from "react";

const STOCKS = {
  "005930": { code: "005930", name: "삼성전자", sector: "반도체", price: "72,400", change: +2.3 },
  "000660": { code: "000660", name: "SK하이닉스", sector: "반도체", price: "185,200", change: +3.1 },
  "035420": { code: "035420", name: "NAVER", sector: "인터넷", price: "214,500", change: +0.8 },
  "051910": { code: "051910", name: "LG화학", sector: "화학", price: "387,000", change: -2.5 },
  "006400": { code: "006400", name: "삼성SDI", sector: "2차전지", price: "412,000", change: +1.7 },
};

const TYPE_STYLES = {
  "실적": { color: "#92400E", bg: "#FEF3C7", icon: "📊" },
  "사업": { color: "#1E40AF", bg: "#DBEAFE", icon: "🚀" },
  "산업": { color: "#3730A3", bg: "#E0E7FF", icon: "🏭" },
  "구조개편": { color: "#991B1B", bg: "#FEE2E2", icon: "🔄" },
  "기술": { color: "#065F46", bg: "#D1FAE5", icon: "⚡" },
};

// Session: "pre" (장전 ~09:00), "open" (장중 09:00~15:30), "post" (장후 15:30~)
// Times are fictional "today" — current time is ~16:00

const FEED = [
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


// ─── Session Divider ───

function SessionDivider({ item }) {
  const sessionStyles = {
    post: { color: "#6B7280", dotColor: "#9CA3AF", bg: "transparent" },
    open: { color: "#059669", dotColor: "#10B981", bg: "rgba(16,185,129,0.04)" },
    pre: { color: "#D97706", dotColor: "#F59E0B", bg: "rgba(245,158,11,0.04)" },
    yesterday: { color: "#9CA3AF", dotColor: "#D1D5DB", bg: "transparent" },
  };
  const s = sessionStyles[item.session] || sessionStyles.post;

  return (
    <div style={{
      display: "flex",
      alignItems: "center",
      padding: "6px 24px",
      gap: "8px",
      margin: "4px 0",
    }}>
      <div style={{ flex: 1, height: "1px", backgroundColor: "#F0F0F0" }} />
      <div style={{
        display: "flex",
        alignItems: "center",
        gap: "5px",
        padding: "3px 10px",
        borderRadius: "10px",
        backgroundColor: s.bg || "transparent",
      }}>
        <div style={{
          width: "5px", height: "5px", borderRadius: "50%",
          backgroundColor: s.dotColor,
        }} />
        <span style={{
          fontSize: "11px",
          fontWeight: "600",
          color: s.color,
          letterSpacing: "0.01em",
        }}>{item.label}</span>
        <span style={{ fontSize: "10px", color: "#CCC" }}>{item.timeRange}</span>
      </div>
      <div style={{ flex: 1, height: "1px", backgroundColor: "#F0F0F0" }} />
    </div>
  );
}

// ─── Time Display Component ───

function TimeDisplay({ time }) {
  if (!time) return null;

  const sessionLabel = {
    pre: { text: "장전", color: "#D97706" },
    open: { text: "장중", color: "#059669" },
    post: { text: "장후", color: "#6B7280" },
  };
  const s = sessionLabel[time.session];

  return (
    <span style={{ display: "inline-flex", alignItems: "center", gap: "4px" }}>
      {s && (
        <span style={{
          fontSize: "9px",
          fontWeight: "700",
          color: s.color,
          letterSpacing: "0.02em",
        }}>{s.text}</span>
      )}
      <span style={{ fontSize: "11px", color: "#AAA" }}>{time.display}</span>
    </span>
  );
}


// ─── Community Components ───

function HighlightComment({ comment }) {
  return (
    <div style={{ display: "flex", gap: "8px", padding: "8px 0" }}>
      <div style={{
        width: "28px", height: "28px", borderRadius: "14px",
        backgroundColor: "#F0F0F0",
        display: "flex", alignItems: "center", justifyContent: "center",
        fontSize: "12px", fontWeight: "700", color: "#999", flexShrink: 0,
      }}>{comment.user.charAt(0)}</div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: "flex", alignItems: "center", gap: "6px", marginBottom: "2px" }}>
          <span style={{ fontSize: "11px", fontWeight: "600", color: "#444" }}>{comment.user}</span>
          <span style={{ fontSize: "10px", color: "#CCC" }}>{comment.time}</span>
        </div>
        <p style={{ margin: 0, fontSize: "12.5px", color: "#555", lineHeight: "1.5", wordBreak: "keep-all" }}>{comment.text}</p>
        <div style={{ display: "flex", alignItems: "center", gap: "4px", marginTop: "4px" }}>
          <span style={{ fontSize: "10px", color: "#CCC" }}>♡</span>
          <span style={{ fontSize: "10px", color: "#BBB" }}>{comment.likes}</span>
        </div>
      </div>
    </div>
  );
}

function PollWidget({ poll, compact }) {
  const [voted, setVoted] = useState(null);
  const totalVotes = poll.options.reduce((a, b) => a + b.votes, 0);
  return (
    <div>
      <div style={{ fontSize: "12px", fontWeight: "600", color: "#444", marginBottom: "8px" }}>{poll.question}</div>
      <div style={{ display: "flex", gap: "6px" }}>
        {poll.options.map((opt, i) => {
          const pct = Math.round((opt.votes / totalVotes) * 100);
          const isSelected = voted === i;
          const showResult = voted !== null;
          return (
            <button key={i} onClick={() => setVoted(i)} style={{
              flex: 1, padding: compact ? "6px 4px" : "8px 6px",
              borderRadius: "8px",
              border: isSelected ? "2px solid #1A1A1A" : "1.5px solid #E5E5E5",
              backgroundColor: showResult ? "#FAFAFA" : "#FFF",
              cursor: voted !== null ? "default" : "pointer",
              textAlign: "center", position: "relative", overflow: "hidden",
            }}>
              {showResult && (
                <div style={{
                  position: "absolute", left: 0, top: 0, bottom: 0,
                  width: `${pct}%`,
                  backgroundColor: isSelected ? "rgba(26,26,26,0.06)" : "rgba(0,0,0,0.02)",
                  transition: "width 0.5s ease",
                }} />
              )}
              <div style={{ position: "relative" }}>
                <span style={{ fontSize: compact ? "14px" : "16px" }}>{opt.emoji}</span>
                <div style={{ fontSize: "11px", fontWeight: isSelected ? "700" : "500", color: isSelected ? "#1A1A1A" : "#777", marginTop: "2px" }}>{opt.label}</div>
                {showResult && <div style={{ fontSize: "11px", fontWeight: "700", color: "#1A1A1A", marginTop: "1px" }}>{pct}%</div>}
              </div>
            </button>
          );
        })}
      </div>
      {voted !== null && <div style={{ fontSize: "10px", color: "#CCC", textAlign: "right", marginTop: "4px" }}>{totalVotes.toLocaleString()}명 참여</div>}
    </div>
  );
}

function ReactionBar({ reactions }) {
  return (
    <div style={{ display: "flex", gap: "6px" }}>
      {reactions.map((r, i) => (
        <button key={i} style={{
          display: "flex", alignItems: "center", gap: "4px",
          padding: "5px 10px", borderRadius: "16px",
          border: "1.5px solid #EBEBEB", backgroundColor: "#FAFAFA",
          cursor: "pointer", fontSize: "12px",
        }}>
          <span style={{ fontSize: "13px" }}>{r.emoji}</span>
          <span style={{ color: "#888", fontWeight: "500" }}>{r.label}</span>
          <span style={{ color: "#BBB", fontWeight: "600", fontSize: "11px" }}>{r.count}</span>
        </button>
      ))}
    </div>
  );
}

function CommunitySection({ community }) {
  if (!community) return null;
  const hasComments = community.highlightComments && community.highlightComments.length > 0;
  const hasPoll = community.poll;
  const hasReactions = community.reactions;

  return (
    <div style={{ borderTop: "1px solid #F3F3F3", backgroundColor: "#FCFCFC" }}>
      {hasReactions && (
        <div style={{ padding: "10px 14px", borderBottom: hasComments || hasPoll ? "1px solid #F3F3F3" : "none" }}>
          <ReactionBar reactions={community.reactions} />
        </div>
      )}
      {hasPoll && (
        <div style={{ padding: "10px 14px", borderBottom: hasComments ? "1px solid #F3F3F3" : "none" }}>
          <PollWidget poll={community.poll} compact={community.type === "poll_only"} />
        </div>
      )}
      {hasComments && (
        <div style={{ padding: "6px 14px 4px" }}>
          {community.highlightComments.map((c, i) => <HighlightComment key={i} comment={c} />)}
        </div>
      )}
      {community.totalComments > 0 && (
        <div style={{ padding: "8px 14px 10px" }}>
          <span style={{ fontSize: "12px", color: "#999", cursor: "pointer" }}>💬 댓글 {community.totalComments}개 보기</span>
        </div>
      )}
    </div>
  );
}


// ─── Card Components ───

function EventCard({ item }) {
  const style = TYPE_STYLES[item.eventType] || TYPE_STYLES["사업"];
  const allStocks = [...item.stocks, ...(item.relatedStocks || [])];

  return (
    <div style={{
      margin: "0 16px 12px", borderRadius: "14px",
      border: "1px solid #EEEEEE", overflow: "hidden", backgroundColor: "#FFF",
    }}>
      {item.hasImage && (
        <div style={{
          height: "150px", backgroundColor: "#F0F0F0",
          position: "relative", overflow: "hidden",
        }}>
          <div style={{
            width: "100%", height: "100%",
            background: item.eventType === "실적"
              ? "linear-gradient(135deg, #1a1a2e 0%, #16213e 40%, #0f3460 70%, #533483 100%)"
              : "linear-gradient(135deg, #0c0c1d 0%, #1b2838 40%, #2d4a5e 70%, #1a6b7a 100%)",
          }}>
            <svg width="100%" height="100%" viewBox="0 0 400 150" preserveAspectRatio="none" style={{ opacity: 0.15 }}>
              <circle cx="300" cy="30" r="80" fill="white"/>
              <circle cx="340" cy="100" r="50" fill="white"/>
              <circle cx="100" cy="120" r="40" fill="white"/>
            </svg>
          </div>
          <div style={{
            position: "absolute", bottom: 0, left: 0, right: 0, height: "60px",
            background: "linear-gradient(transparent, rgba(0,0,0,0.6))",
          }} />
          <div style={{
            position: "absolute", bottom: "10px", left: "12px",
            display: "flex", gap: "6px", alignItems: "center",
          }}>
            <span style={{
              fontSize: "11px", fontWeight: "600", color: "#FFF",
              backgroundColor: "rgba(255,255,255,0.2)", backdropFilter: "blur(8px)",
              padding: "3px 8px", borderRadius: "5px",
            }}>{style.icon} {item.eventType}</span>
            <TimeDisplay time={item.time} />
          </div>
          <span style={{
            position: "absolute", top: "10px", right: "12px",
            fontSize: "10px", color: "rgba(255,255,255,0.5)",
            backgroundColor: "rgba(0,0,0,0.3)",
            padding: "2px 6px", borderRadius: "4px",
          }}>기사 {item.articleCount}건</span>
        </div>
      )}

      <div style={{ padding: item.hasImage ? "12px 14px 0" : "14px 14px 0" }}>
        {!item.hasImage && (
          <div style={{ display: "flex", alignItems: "center", gap: "6px", marginBottom: "8px" }}>
            <span style={{ fontSize: "14px" }}>{style.icon}</span>
            <span style={{
              fontSize: "11px", fontWeight: "600", color: style.color,
              backgroundColor: style.bg, padding: "2px 7px", borderRadius: "4px",
            }}>{item.eventType}</span>
            <TimeDisplay time={item.time} />
            <span style={{ marginLeft: "auto", fontSize: "11px", color: "#CCC" }}>기사 {item.articleCount}건</span>
          </div>
        )}
        <h3 style={{
          margin: "0 0 6px", fontSize: "15.5px", fontWeight: "700",
          color: "#1A1A1A", lineHeight: "1.45",
          letterSpacing: "-0.02em", wordBreak: "keep-all",
        }}>{item.headline}</h3>
        <p style={{
          margin: "0 0 12px", fontSize: "13px",
          color: "#888", lineHeight: "1.5", wordBreak: "keep-all",
        }}>{item.summary}</p>
      </div>

      <div style={{
        borderTop: "1px solid #F3F3F3", backgroundColor: "#FAFAFA", padding: "10px 14px",
      }}>
        {allStocks.map(s => {
          const stock = STOCKS[s.code];
          const up = s.reaction > 0;
          const isDirect = item.stocks.some(es => es.code === s.code);
          return (
            <div key={s.code} style={{
              display: "flex", alignItems: "center",
              padding: "3px 0", gap: "8px", opacity: isDirect ? 1 : 0.55,
            }}>
              <span style={{ fontSize: "13px", fontWeight: "600", color: "#1A1A1A", width: "72px" }}>{stock.name}</span>
              <span style={{ fontSize: "10px", color: "#AAA", width: "52px" }}>{s.role}</span>
              <div style={{ flex: 1, display: "flex", alignItems: "center", gap: "6px" }}>
                <div style={{ flex: 1, height: "4px", borderRadius: "2px", backgroundColor: "#EAEAEA", overflow: "hidden" }}>
                  <div style={{
                    width: `${Math.min(Math.abs(s.reaction) * 20, 100)}%`,
                    height: "100%", borderRadius: "2px",
                    backgroundColor: up ? "#EF4444" : "#3B82F6",
                  }} />
                </div>
                <span style={{
                  fontSize: "12px", fontWeight: "700", width: "48px", textAlign: "right",
                  color: up ? "#DC2626" : "#2563EB",
                }}>{up ? "+" : ""}{s.reaction}%</span>
              </div>
            </div>
          );
        })}
      </div>

      <CommunitySection community={item.community} />
    </div>
  );
}

function SectorMoodCard({ item }) {
  const isHot = item.mood === "hot";
  return (
    <div style={{
      margin: "0 16px 12px", borderRadius: "14px", padding: "14px 16px",
      background: isHot
        ? "linear-gradient(135deg, #FFF7ED 0%, #FEF3C7 100%)"
        : "linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%)",
      border: `1px solid ${isHot ? "#FDE68A" : "#BFDBFE"}`,
    }}>
      <div style={{ display: "flex", alignItems: "center", gap: "8px", marginBottom: "2px" }}>
        <span style={{ fontSize: "18px" }}>{item.emoji}</span>
        <span style={{ fontSize: "14px", fontWeight: "700", color: "#1A1A1A" }}>{item.sector} 섹터</span>
        <span style={{
          fontSize: "10px", fontWeight: "600",
          color: isHot ? "#EA580C" : "#2563EB",
          backgroundColor: isHot ? "rgba(234,88,12,0.1)" : "rgba(37,99,235,0.1)",
          padding: "2px 8px", borderRadius: "10px",
        }}>{isHot ? "HOT" : "COOL"}</span>
        {item.time && <TimeDisplay time={item.time} />}
      </div>
      <div style={{ fontSize: "12px", color: "#666", marginBottom: "10px", marginTop: "6px" }}>{item.summary}</div>
      <div style={{ display: "flex", gap: "8px" }}>
        {item.stocks.map(s => {
          const stock = STOCKS[s.code];
          const up = s.change > 0;
          return (
            <div key={s.code} style={{
              flex: 1, padding: "8px 10px", borderRadius: "8px",
              backgroundColor: "rgba(255,255,255,0.7)", border: "1px solid rgba(0,0,0,0.04)",
            }}>
              <div style={{ fontSize: "12px", fontWeight: "600", color: "#1A1A1A" }}>{stock.name}</div>
              <div style={{ fontSize: "14px", fontWeight: "700", marginTop: "2px", color: up ? "#DC2626" : "#2563EB" }}>
                {up ? "+" : ""}{s.change}%
              </div>
            </div>
          );
        })}
      </div>
      <div style={{ fontSize: "11px", color: "#999", marginTop: "8px" }}>{item.detail}</div>
    </div>
  );
}

function HistoryCard({ item }) {
  const stock = STOCKS[item.stockCode];
  return (
    <div style={{
      margin: "0 16px 12px", borderRadius: "14px",
      border: "1px solid #F0F0F0", overflow: "hidden",
    }}>
      <div style={{ padding: "14px 14px 10px" }}>
        <div style={{ display: "flex", alignItems: "center", gap: "6px", marginBottom: "8px" }}>
          <span style={{ fontSize: "14px" }}>🔁</span>
          <span style={{ fontSize: "11px", fontWeight: "600", color: "#6B21A8", backgroundColor: "#F3E8FF", padding: "2px 7px", borderRadius: "4px" }}>과거 비교</span>
          <span style={{ fontSize: "12px", fontWeight: "600", color: "#1A1A1A" }}>{stock.name}</span>
        </div>
        <div style={{ fontSize: "13px", fontWeight: "600", color: "#444", marginBottom: "10px" }}>{item.title}</div>
      </div>
      <div style={{ padding: "0 14px 12px", display: "flex", flexDirection: "column", gap: "8px" }}>
        {[item.current, item.past].map((d, i) => {
          const up = d.reaction > 0;
          const isCurrent = i === 0;
          return (
            <div key={i} style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <span style={{ fontSize: "11px", color: isCurrent ? "#666" : "#999", width: "80px" }}>{d.label}</span>
              <div style={{ flex: 1, height: "22px", backgroundColor: "#F5F5F5", borderRadius: "6px", overflow: "hidden", position: "relative" }}>
                <div style={{
                  height: "100%",
                  width: `${Math.min(Math.abs(d.reaction) * 15, 95)}%`,
                  borderRadius: "6px",
                  backgroundColor: up ? (isCurrent ? "#FCA5A5" : "#FECACA") : (isCurrent ? "#93C5FD" : "#BFDBFE"),
                  opacity: isCurrent ? 1 : 0.6,
                  display: "flex", alignItems: "center", paddingLeft: "8px",
                }}>
                  <span style={{ fontSize: "11px", fontWeight: "700", color: up ? "#991B1B" : "#1E3A8A" }}>
                    {up ? "+" : ""}{d.reaction}%
                  </span>
                </div>
                <span style={{ position: "absolute", right: "8px", top: "50%", transform: "translateY(-50%)", fontSize: "10px", color: "#BBB" }}>{d.date}</span>
              </div>
            </div>
          );
        })}
      </div>
      <div style={{ padding: "10px 14px", borderTop: "1px solid #F3F3F3", backgroundColor: "#FAFAFA" }}>
        <div style={{ fontSize: "12px", color: "#777", lineHeight: "1.5", wordBreak: "keep-all" }}>💡 {item.insight}</div>
      </div>
    </div>
  );
}

function AnomalyCard({ item }) {
  const stock = STOCKS[item.stockCode];
  const maxVal = Math.max(...item.trend);
  return (
    <div style={{
      margin: "0 16px 12px", borderRadius: "14px",
      border: "1px solid #FEE2E2", overflow: "hidden", backgroundColor: "#FFFBFB",
    }}>
      <div style={{ padding: "14px 14px 10px" }}>
        <div style={{ display: "flex", alignItems: "center", gap: "6px", marginBottom: "6px" }}>
          <span style={{ fontSize: "14px" }}>{item.emoji}</span>
          <span style={{ fontSize: "11px", fontWeight: "600", color: "#DC2626", backgroundColor: "#FEE2E2", padding: "2px 7px", borderRadius: "4px" }}>이상 신호</span>
          <span style={{ fontSize: "12px", fontWeight: "600", color: "#1A1A1A" }}>{stock.name}</span>
        </div>
        <div style={{ fontSize: "14px", fontWeight: "700", color: "#1A1A1A", marginBottom: "4px" }}>{item.title}</div>
        <div style={{ fontSize: "20px", fontWeight: "800", color: "#DC2626", letterSpacing: "-0.03em" }}>{item.stat}</div>
      </div>
      <div style={{ padding: "0 14px 8px", height: "40px", display: "flex", alignItems: "flex-end", gap: "3px" }}>
        {item.trend.map((v, i) => (
          <div key={i} style={{
            flex: 1, height: `${(v / maxVal) * 100}%`,
            backgroundColor: i === item.trend.length - 1 ? "#EF4444" : i >= item.trend.length - 3 ? "#FCA5A5" : "#E5E5E5",
            borderRadius: "2px", minHeight: "3px",
          }} />
        ))}
      </div>
      <div style={{ padding: "10px 14px", borderTop: "1px solid #FEE2E2", fontSize: "12px", color: "#999" }}>{item.detail}</div>
    </div>
  );
}

function ConnectionCard({ item }) {
  const stock1 = STOCKS[item.pair[0]];
  const stock2 = STOCKS[item.pair[1]];
  return (
    <div style={{
      margin: "0 16px 12px", borderRadius: "14px",
      border: "1px solid #E0E7FF", overflow: "hidden",
      backgroundColor: "#F8F9FF", padding: "14px",
    }}>
      <div style={{ display: "flex", alignItems: "center", gap: "6px", marginBottom: "10px" }}>
        <span style={{ fontSize: "14px" }}>🔗</span>
        <span style={{ fontSize: "11px", fontWeight: "600", color: "#3730A3", backgroundColor: "#E0E7FF", padding: "2px 7px", borderRadius: "4px" }}>종목 연결</span>
      </div>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "center", padding: "4px 0 10px" }}>
        <div style={{ padding: "8px 14px", borderRadius: "10px", backgroundColor: "#FFF", border: "1.5px solid #C7D2FE", textAlign: "center" }}>
          <div style={{ fontSize: "13px", fontWeight: "700", color: "#1A1A1A" }}>{stock1.name}</div>
          <div style={{ fontSize: "10px", color: "#999", marginTop: "1px" }}>{stock1.sector}</div>
        </div>
        <div style={{ width: "50px", display: "flex", flexDirection: "column", alignItems: "center", position: "relative" }}>
          <div style={{ width: "100%", height: "2px", backgroundColor: "#A5B4FC" }} />
          <div style={{ position: "absolute", top: "-9px", fontSize: "10px", fontWeight: "700", color: "#4F46E5", backgroundColor: "#F8F9FF", padding: "0 4px" }}>{item.count}회</div>
        </div>
        <div style={{ padding: "8px 14px", borderRadius: "10px", backgroundColor: "#FFF", border: "1.5px solid #C7D2FE", textAlign: "center" }}>
          <div style={{ fontSize: "13px", fontWeight: "700", color: "#1A1A1A" }}>{stock2.name}</div>
          <div style={{ fontSize: "10px", color: "#999", marginTop: "1px" }}>{stock2.sector}</div>
        </div>
      </div>
      <div style={{ fontSize: "12px", color: "#777", marginBottom: "8px", textAlign: "center" }}>{item.period} 동시 언급 {item.count}회</div>
      <div style={{ display: "flex", gap: "4px", flexWrap: "wrap", justifyContent: "center" }}>
        {item.topKeywords.map(k => (
          <span key={k} style={{ fontSize: "10px", color: "#6366F1", padding: "2px 8px", borderRadius: "10px", backgroundColor: "#EEF2FF" }}>#{k}</span>
        ))}
      </div>
    </div>
  );
}

function StatCard({ item }) {
  return (
    <div style={{
      margin: "0 16px 12px", borderRadius: "14px",
      border: "1px solid #F0F0F0", padding: "14px",
    }}>
      <div style={{ display: "flex", alignItems: "center", gap: "6px", marginBottom: "12px" }}>
        <span style={{ fontSize: "16px" }}>{item.emoji}</span>
        <span style={{ fontSize: "13px", fontWeight: "700", color: "#1A1A1A" }}>{item.title}</span>
      </div>
      {item.items.map((s, i) => {
        const stock = STOCKS[s.code];
        return (
          <div key={s.code} style={{
            display: "flex", alignItems: "center", padding: "6px 0", gap: "10px",
            borderBottom: i < item.items.length - 1 ? "1px solid #F5F5F5" : "none",
          }}>
            <span style={{ fontSize: i === 0 ? "18px" : "14px", width: "28px", textAlign: "center" }}>
              {i === 0 ? "🥇" : i === 1 ? "🥈" : "🥉"}
            </span>
            <span style={{ fontSize: "13px", fontWeight: "600", color: "#1A1A1A", flex: 1 }}>{stock.name}</span>
            <span style={{ fontSize: "13px", fontWeight: "700", color: "#666" }}>{s.newsCount}건</span>
          </div>
        );
      })}
    </div>
  );
}

// ─── Main ───

function StatusBar() {
  return (
    <div style={{
      display: "flex", justifyContent: "space-between", alignItems: "center",
      padding: "8px 20px 4px", fontSize: "12px", fontWeight: "600", color: "#1A1A1A",
    }}>
      <span>9:41</span>
      <div style={{ display: "flex", gap: "5px", alignItems: "center" }}>
        <svg width="16" height="12" viewBox="0 0 16 12"><rect x="0" y="6" width="3" height="6" rx="0.5" fill="#1A1A1A"/><rect x="4.5" y="4" width="3" height="8" rx="0.5" fill="#1A1A1A"/><rect x="9" y="1.5" width="3" height="10.5" rx="0.5" fill="#1A1A1A"/><rect x="13" y="0" width="3" height="12" rx="0.5" fill="#1A1A1A"/></svg>
        <svg width="25" height="12" viewBox="0 0 25 12"><rect x="0" y="1" width="22" height="10" rx="2" stroke="#1A1A1A" strokeWidth="1" fill="none"/><rect x="1.5" y="2.5" width="16" height="7" rx="1" fill="#1A1A1A"/></svg>
      </div>
    </div>
  );
}

function renderCard(item) {
  switch (item.type) {
    case "session_divider": return <SessionDivider key={item.id} item={item} />;
    case "event": return <EventCard key={item.id} item={item} />;
    case "sector_mood": return <SectorMoodCard key={item.id} item={item} />;
    case "history": return <HistoryCard key={item.id} item={item} />;
    case "anomaly": return <AnomalyCard key={item.id} item={item} />;
    case "connection": return <ConnectionCard key={item.id} item={item} />;
    case "stat": return <StatCard key={item.id} item={item} />;
    default: return null;
  }
}

export default function StockerFeedV4() {
  return (
    <div style={{
      display: "flex", justifyContent: "center", alignItems: "flex-start",
      minHeight: "100vh", backgroundColor: "#E8E8E8",
      fontFamily: "'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
      padding: "20px",
    }}>
      <div style={{
        width: "375px", height: "812px",
        backgroundColor: "#FFFFFF", borderRadius: "44px",
        overflow: "hidden",
        boxShadow: "0 25px 60px rgba(0,0,0,0.2), 0 0 0 1px rgba(0,0,0,0.08)",
        display: "flex", flexDirection: "column", position: "relative",
      }}>
        <StatusBar />
        <div style={{ width: "120px", height: "34px", backgroundColor: "#000", borderRadius: "20px", margin: "0 auto 4px" }} />

        <div style={{
          padding: "12px 20px 10px", borderBottom: "1px solid #F0F0F0",
          display: "flex", alignItems: "center", justifyContent: "space-between",
        }}>
          <h1 style={{ margin: 0, fontSize: "22px", fontWeight: "800", color: "#1A1A1A", letterSpacing: "-0.03em" }}>피드</h1>
          <span style={{ fontSize: "12px", color: "#BBB" }}>5종목 구독중</span>
        </div>

        <div style={{ flex: 1, overflowY: "auto", WebkitOverflowScrolling: "touch", paddingTop: "8px", paddingBottom: "40px" }}>
          {FEED.map(item => renderCard(item))}
        </div>

        <div style={{
          display: "flex", borderTop: "1px solid #F0F0F0",
          paddingBottom: "24px", backgroundColor: "rgba(255,255,255,0.97)",
        }}>
          {[
            { label: "피드", active: true, icon: "📰" },
            { label: "관심종목", active: false, icon: "⭐" },
            { label: "설정", active: false, icon: "⚙️" },
          ].map(t => (
            <div key={t.label} style={{
              flex: 1, display: "flex", flexDirection: "column",
              alignItems: "center", gap: "2px", padding: "8px 0 0",
            }}>
              <span style={{ fontSize: "18px", opacity: t.active ? 1 : 0.4 }}>{t.icon}</span>
              <span style={{ fontSize: "10px", fontWeight: t.active ? "600" : "400", color: t.active ? "#1A1A1A" : "#AAA" }}>{t.label}</span>
            </div>
          ))}
        </div>

        <div style={{
          position: "absolute", bottom: "6px", left: "50%",
          transform: "translateX(-50%)", width: "134px", height: "4px",
          borderRadius: "2px", backgroundColor: "#1A1A1A", opacity: 0.2,
        }} />
      </div>
    </div>
  );
}
