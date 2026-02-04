import React, { useState } from "react";
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  TouchableOpacity,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { LinearGradient } from "expo-linear-gradient";
import { useTheme } from "./theme";
import {
  STOCKS,
  TYPE_STYLES,
  FEED,
  FeedItem,
  EventItem,
  SectorMoodItem,
  HistoryItem,
  AnomalyItem,
  ConnectionItem,
  StatItem,
  SessionDividerItem,
  CommunityData,
  HighlightCommentData,
  PollData,
  ReactionData,
  TimeInfo,
} from "./feedData";

// ─── Dark overrides for event type badges ───

const DARK_TYPE_STYLES: Record<string, { color: string; bg: string; icon: string }> = {
  "실적": { color: "#FCD34D", bg: "#42200680", icon: "📊" },
  "사업": { color: "#93C5FD", bg: "#1E3A5C80", icon: "🚀" },
  "산업": { color: "#A5B4FC", bg: "#2D2D4A80", icon: "🏭" },
  "구조개편": { color: "#FCA5A5", bg: "#3D202080", icon: "🔄" },
  "기술": { color: "#6EE7B7", bg: "#064E3B80", icon: "⚡" },
};

// ─── Time Display ───

function TimeDisplay({ time }: { time?: TimeInfo }) {
  const { colors, isDark } = useTheme();
  if (!time) return null;

  const sessionLabel: Record<string, { text: string; color: string }> = {
    pre: { text: "장전", color: isDark ? "#FBBF24" : "#D97706" },
    open: { text: "장중", color: isDark ? "#34D399" : "#059669" },
    post: { text: "장후", color: isDark ? "#9CA3AF" : "#6B7280" },
  };
  const s = sessionLabel[time.session];

  return (
    <View style={{ flexDirection: "row", alignItems: "center", gap: 4 }}>
      {s && (
        <Text style={{ fontSize: 9, fontWeight: "700", color: s.color, letterSpacing: 0.2 }}>
          {s.text}
        </Text>
      )}
      <Text style={{ fontSize: 11, color: colors.textFaint }}>{time.display}</Text>
    </View>
  );
}

// ─── Session Divider ───

function SessionDivider({ item }: { item: SessionDividerItem }) {
  const { colors, isDark } = useTheme();

  const sessionStyles: Record<string, { color: string; dotColor: string; bg: string }> = {
    post: { color: isDark ? "#9CA3AF" : "#6B7280", dotColor: "#9CA3AF", bg: "transparent" },
    open: { color: isDark ? "#34D399" : "#059669", dotColor: isDark ? "#6EE7B7" : "#10B981", bg: isDark ? "rgba(16,185,129,0.08)" : "rgba(16,185,129,0.04)" },
    pre: { color: isDark ? "#FBBF24" : "#D97706", dotColor: isDark ? "#FCD34D" : "#F59E0B", bg: isDark ? "rgba(245,158,11,0.08)" : "rgba(245,158,11,0.04)" },
    yesterday: { color: isDark ? "#636366" : "#9CA3AF", dotColor: isDark ? "#48484A" : "#D1D5DB", bg: "transparent" },
  };
  const s = sessionStyles[item.session] || sessionStyles.post;

  return (
    <View style={styles.sessionDivider}>
      <View style={[styles.sessionLine, { backgroundColor: colors.divider }]} />
      <View style={[styles.sessionLabel, { backgroundColor: s.bg }]}>
        <View style={[styles.sessionDot, { backgroundColor: s.dotColor }]} />
        <Text style={[styles.sessionText, { color: s.color }]}>{item.label}</Text>
        <Text style={[styles.sessionTimeRange, { color: colors.textFaint }]}>{item.timeRange}</Text>
      </View>
      <View style={[styles.sessionLine, { backgroundColor: colors.divider }]} />
    </View>
  );
}

// ─── Community Components ───

function HighlightComment({ comment }: { comment: HighlightCommentData }) {
  const { colors } = useTheme();

  return (
    <View style={styles.commentRow}>
      <View style={[styles.commentAvatar, { backgroundColor: colors.avatarBg }]}>
        <Text style={[styles.commentAvatarText, { color: colors.textMuted }]}>
          {comment.user.charAt(0)}
        </Text>
      </View>
      <View style={{ flex: 1 }}>
        <View style={{ flexDirection: "row", alignItems: "center", gap: 6, marginBottom: 2 }}>
          <Text style={{ fontSize: 11, fontWeight: "600", color: colors.textSecondary }}>
            {comment.user}
          </Text>
          <Text style={{ fontSize: 10, color: colors.textFaint }}>{comment.time}</Text>
        </View>
        <Text style={{ fontSize: 12.5, color: colors.textSecondary, lineHeight: 19 }}>
          {comment.text}
        </Text>
        <View style={{ flexDirection: "row", alignItems: "center", gap: 4, marginTop: 4 }}>
          <Text style={{ fontSize: 10, color: colors.textFaint }}>♡</Text>
          <Text style={{ fontSize: 10, color: colors.textFaint }}>{comment.likes}</Text>
        </View>
      </View>
    </View>
  );
}

function PollWidget({ poll, compact }: { poll: PollData; compact?: boolean }) {
  const [voted, setVoted] = useState<number | null>(null);
  const { colors, isDark } = useTheme();
  const totalVotes = poll.options.reduce((a, b) => a + b.votes, 0);

  return (
    <View>
      <Text style={{ fontSize: 12, fontWeight: "600", color: colors.textSecondary, marginBottom: 8 }}>
        {poll.question}
      </Text>
      <View style={{ flexDirection: "row", gap: 6 }}>
        {poll.options.map((opt, i) => {
          const pct = Math.round((opt.votes / totalVotes) * 100);
          const isSelected = voted === i;
          const showResult = voted !== null;
          return (
            <TouchableOpacity
              key={i}
              onPress={() => setVoted(i)}
              disabled={voted !== null}
              activeOpacity={0.7}
              style={{
                flex: 1,
                padding: compact ? 6 : 8,
                borderRadius: 8,
                borderWidth: isSelected ? 2 : 1.5,
                borderColor: isSelected ? colors.text : colors.track,
                backgroundColor: showResult ? colors.surface : colors.card,
                alignItems: "center",
                overflow: "hidden",
              }}
            >
              {showResult && (
                <View
                  style={{
                    position: "absolute",
                    left: 0,
                    top: 0,
                    bottom: 0,
                    width: `${pct}%`,
                    backgroundColor: isSelected
                      ? isDark ? "rgba(255,255,255,0.08)" : "rgba(26,26,26,0.06)"
                      : isDark ? "rgba(255,255,255,0.03)" : "rgba(0,0,0,0.02)",
                  }}
                />
              )}
              <Text style={{ fontSize: compact ? 14 : 16 }}>{opt.emoji}</Text>
              <Text
                style={{
                  fontSize: 11,
                  fontWeight: isSelected ? "700" : "500",
                  color: isSelected ? colors.text : colors.textTertiary,
                  marginTop: 2,
                }}
              >
                {opt.label}
              </Text>
              {showResult && (
                <Text style={{ fontSize: 11, fontWeight: "700", color: colors.text, marginTop: 1 }}>
                  {pct}%
                </Text>
              )}
            </TouchableOpacity>
          );
        })}
      </View>
      {voted !== null && (
        <Text style={{ fontSize: 10, color: colors.textFaint, textAlign: "right", marginTop: 4 }}>
          {totalVotes.toLocaleString()}명 참여
        </Text>
      )}
    </View>
  );
}

function ReactionBar({ reactions }: { reactions: ReactionData[] }) {
  const { colors } = useTheme();

  return (
    <View style={{ flexDirection: "row", gap: 6 }}>
      {reactions.map((r, i) => (
        <TouchableOpacity
          key={i}
          activeOpacity={0.7}
          style={[styles.reactionButton, { borderColor: colors.border, backgroundColor: colors.surface }]}
        >
          <Text style={{ fontSize: 13 }}>{r.emoji}</Text>
          <Text style={{ color: colors.textMuted, fontWeight: "500", fontSize: 12 }}>{r.label}</Text>
          <Text style={{ color: colors.textFaint, fontWeight: "600", fontSize: 11 }}>{r.count}</Text>
        </TouchableOpacity>
      ))}
    </View>
  );
}

function CommunitySection({ community }: { community?: CommunityData }) {
  const { colors } = useTheme();
  if (!community) return null;
  const hasComments = community.highlightComments && community.highlightComments.length > 0;
  const hasPoll = !!community.poll;
  const hasReactions = !!community.reactions;

  return (
    <View style={{ borderTopWidth: 1, borderTopColor: colors.divider, backgroundColor: colors.surface }}>
      {hasReactions && (
        <View
          style={{
            padding: 10,
            paddingHorizontal: 14,
            borderBottomWidth: hasComments || hasPoll ? 1 : 0,
            borderBottomColor: colors.divider,
          }}
        >
          <ReactionBar reactions={community.reactions!} />
        </View>
      )}
      {hasPoll && (
        <View
          style={{
            padding: 10,
            paddingHorizontal: 14,
            borderBottomWidth: hasComments ? 1 : 0,
            borderBottomColor: colors.divider,
          }}
        >
          <PollWidget poll={community.poll!} compact={community.type === "poll_only"} />
        </View>
      )}
      {hasComments && (
        <View style={{ paddingHorizontal: 14, paddingTop: 6, paddingBottom: 4 }}>
          {community.highlightComments!.map((c, i) => (
            <HighlightComment key={i} comment={c} />
          ))}
        </View>
      )}
      {community.totalComments > 0 && (
        <View style={{ paddingHorizontal: 14, paddingTop: 8, paddingBottom: 10 }}>
          <Text style={{ fontSize: 12, color: colors.textMuted }}>
            💬 댓글 {community.totalComments}개 보기
          </Text>
        </View>
      )}
    </View>
  );
}

// ─── Card Components ───

function EventCard({ item }: { item: EventItem }) {
  const { colors, isDark } = useTheme();
  const typeStyles = isDark ? DARK_TYPE_STYLES : TYPE_STYLES;
  const style = typeStyles[item.eventType] || typeStyles["사업"];
  const allStocks = [...item.stocks, ...(item.relatedStocks || [])];

  return (
    <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
      {item.hasImage && (
        <View style={[styles.eventImageContainer, { backgroundColor: colors.track }]}>
          <LinearGradient
            colors={
              item.eventType === "실적"
                ? ["#1a1a2e", "#16213e", "#0f3460", "#533483"]
                : ["#0c0c1d", "#1b2838", "#2d4a5e", "#1a6b7a"]
            }
            start={{ x: 0, y: 0 }}
            end={{ x: 1, y: 1 }}
            style={StyleSheet.absoluteFill}
          >
            <View style={[styles.decoCircle, { width: 160, height: 160, borderRadius: 80, right: -30, top: -50, opacity: 0.08 }]} />
            <View style={[styles.decoCircle, { width: 100, height: 100, borderRadius: 50, right: -10, top: 60, opacity: 0.06 }]} />
            <View style={[styles.decoCircle, { width: 80, height: 80, borderRadius: 40, left: 50, bottom: -20, opacity: 0.05 }]} />
          </LinearGradient>
          <LinearGradient
            colors={["transparent", "rgba(0,0,0,0.6)"]}
            style={styles.eventImageOverlay}
          />
          <View style={styles.eventImageBadges}>
            <View style={styles.eventTypeBadgeOnImage}>
              <Text style={{ fontSize: 11, fontWeight: "600", color: "#FFF" }}>
                {style.icon} {item.eventType}
              </Text>
            </View>
            <TimeDisplay time={item.time} />
          </View>
          <View style={styles.eventArticleCount}>
            <Text style={{ fontSize: 10, color: "rgba(255,255,255,0.5)" }}>
              기사 {item.articleCount}건
            </Text>
          </View>
        </View>
      )}

      <View style={{ padding: item.hasImage ? 12 : 14, paddingHorizontal: 14, paddingBottom: 0 }}>
        {!item.hasImage && (
          <View style={{ flexDirection: "row", alignItems: "center", gap: 6, marginBottom: 8 }}>
            <Text style={{ fontSize: 14 }}>{style.icon}</Text>
            <View style={{ backgroundColor: style.bg, paddingHorizontal: 7, paddingVertical: 2, borderRadius: 4 }}>
              <Text style={{ fontSize: 11, fontWeight: "600", color: style.color }}>{item.eventType}</Text>
            </View>
            <TimeDisplay time={item.time} />
            <View style={{ flex: 1 }} />
            <Text style={{ fontSize: 11, color: colors.textFaint }}>기사 {item.articleCount}건</Text>
          </View>
        )}
        <Text style={[styles.eventHeadline, { color: colors.text }]}>{item.headline}</Text>
        <Text style={[styles.eventSummary, { color: colors.textMuted }]}>{item.summary}</Text>
      </View>

      {/* Stock reactions */}
      <View style={[styles.stockReactionArea, { borderTopColor: colors.divider, backgroundColor: colors.surface }]}>
        {allStocks.map((s) => {
          const stock = STOCKS[s.code];
          const up = s.reaction > 0;
          const isDirect = item.stocks.some((es) => es.code === s.code);
          return (
            <View
              key={s.code}
              style={[styles.stockReactionRow, { opacity: isDirect ? 1 : 0.55 }]}
            >
              <Text style={[styles.stockName, { color: colors.text }]}>{stock.name}</Text>
              <Text style={[styles.stockRole, { color: colors.textFaint }]}>{s.role}</Text>
              <View style={{ flex: 1, flexDirection: "row", alignItems: "center", gap: 6 }}>
                <View style={[styles.reactionBarTrack, { backgroundColor: colors.track }]}>
                  <View
                    style={[
                      styles.reactionBarFill,
                      {
                        width: `${Math.min(Math.abs(s.reaction) * 20, 100)}%`,
                        backgroundColor: up ? "#EF4444" : "#3B82F6",
                      },
                    ]}
                  />
                </View>
                <Text
                  style={[
                    styles.reactionPercent,
                    { color: up ? "#DC2626" : "#2563EB" },
                  ]}
                >
                  {up ? "+" : ""}
                  {s.reaction}%
                </Text>
              </View>
            </View>
          );
        })}
      </View>

      <CommunitySection community={item.community} />
    </View>
  );
}

function SectorMoodCard({ item }: { item: SectorMoodItem }) {
  const { colors, isDark } = useTheme();
  const isHot = item.mood === "hot";

  const gradientColors: [string, string] = isHot
    ? isDark ? ["#1A1008", "#1A1508"] : ["#FFF7ED", "#FEF3C7"]
    : isDark ? ["#0A1020", "#0D1530"] : ["#EFF6FF", "#DBEAFE"];

  const borderColor = isHot
    ? isDark ? "#5C4A1E" : "#FDE68A"
    : isDark ? "#1E3A5C" : "#BFDBFE";

  return (
    <LinearGradient
      colors={gradientColors}
      start={{ x: 0, y: 0 }}
      end={{ x: 1, y: 1 }}
      style={[styles.sectorMoodCard, { borderColor }]}
    >
      <View style={{ flexDirection: "row", alignItems: "center", gap: 8, marginBottom: 2 }}>
        <Text style={{ fontSize: 18 }}>{item.emoji}</Text>
        <Text style={{ fontSize: 14, fontWeight: "700", color: colors.text }}>
          {item.sector} 섹터
        </Text>
        <View
          style={{
            paddingHorizontal: 8,
            paddingVertical: 2,
            borderRadius: 10,
            backgroundColor: isHot
              ? isDark ? "rgba(251,191,36,0.15)" : "rgba(234,88,12,0.1)"
              : isDark ? "rgba(96,165,250,0.15)" : "rgba(37,99,235,0.1)",
          }}
        >
          <Text
            style={{
              fontSize: 10,
              fontWeight: "600",
              color: isHot
                ? isDark ? "#FBBF24" : "#EA580C"
                : isDark ? "#60A5FA" : "#2563EB",
            }}
          >
            {isHot ? "HOT" : "COOL"}
          </Text>
        </View>
        <TimeDisplay time={item.time} />
      </View>
      <Text style={{ fontSize: 12, color: colors.textTertiary, marginBottom: 10, marginTop: 6 }}>
        {item.summary}
      </Text>
      <View style={{ flexDirection: "row", gap: 8 }}>
        {item.stocks.map((s) => {
          const stock = STOCKS[s.code];
          const up = s.change > 0;
          return (
            <View
              key={s.code}
              style={[
                styles.sectorStockBox,
                {
                  backgroundColor: isDark ? "rgba(255,255,255,0.08)" : "rgba(255,255,255,0.7)",
                  borderColor: isDark ? "rgba(255,255,255,0.1)" : "rgba(0,0,0,0.04)",
                },
              ]}
            >
              <Text style={{ fontSize: 12, fontWeight: "600", color: colors.text }}>
                {stock.name}
              </Text>
              <Text
                style={{
                  fontSize: 14,
                  fontWeight: "700",
                  marginTop: 2,
                  color: up ? "#DC2626" : "#2563EB",
                }}
              >
                {up ? "+" : ""}
                {s.change}%
              </Text>
            </View>
          );
        })}
      </View>
      <Text style={{ fontSize: 11, color: colors.textMuted, marginTop: 8 }}>{item.detail}</Text>
    </LinearGradient>
  );
}

function HistoryCard({ item }: { item: HistoryItem }) {
  const { colors, isDark } = useTheme();
  const stock = STOCKS[item.stockCode];

  const badgeBg = isDark ? "#2D1B4E" : "#F3E8FF";
  const badgeColor = isDark ? "#C084FC" : "#6B21A8";

  return (
    <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
      <View style={{ padding: 14, paddingBottom: 10 }}>
        <View style={{ flexDirection: "row", alignItems: "center", gap: 6, marginBottom: 8 }}>
          <Text style={{ fontSize: 14 }}>🔁</Text>
          <View style={{ backgroundColor: badgeBg, paddingHorizontal: 7, paddingVertical: 2, borderRadius: 4 }}>
            <Text style={{ fontSize: 11, fontWeight: "600", color: badgeColor }}>과거 비교</Text>
          </View>
          <Text style={{ fontSize: 12, fontWeight: "600", color: colors.text }}>{stock.name}</Text>
        </View>
        <Text style={{ fontSize: 13, fontWeight: "600", color: colors.textSecondary, marginBottom: 10 }}>
          {item.title}
        </Text>
      </View>
      <View style={{ paddingHorizontal: 14, paddingBottom: 12, gap: 8 }}>
        {[item.current, item.past].map((d, i) => {
          const up = d.reaction > 0;
          const isCurrent = i === 0;
          const barColor = up
            ? isDark
              ? isCurrent ? "#991B1B" : "#7F1D1D"
              : isCurrent ? "#FCA5A5" : "#FECACA"
            : isDark
              ? isCurrent ? "#1E40AF" : "#1E3A8A"
              : isCurrent ? "#93C5FD" : "#BFDBFE";
          const barTextColor = up
            ? isDark ? "#FCA5A5" : "#991B1B"
            : isDark ? "#93C5FD" : "#1E3A8A";

          return (
            <View key={i} style={{ flexDirection: "row", alignItems: "center", gap: 8 }}>
              <Text
                style={{
                  fontSize: 11,
                  color: isCurrent ? colors.textTertiary : colors.textMuted,
                  width: 80,
                }}
              >
                {d.label}
              </Text>
              <View style={[styles.historyBarTrack, { backgroundColor: colors.track }]}>
                <View
                  style={[
                    styles.historyBarFill,
                    {
                      width: `${Math.min(Math.abs(d.reaction) * 15, 95)}%`,
                      backgroundColor: barColor,
                      opacity: isCurrent ? 1 : 0.6,
                    },
                  ]}
                >
                  <Text
                    style={{
                      fontSize: 11,
                      fontWeight: "700",
                      color: barTextColor,
                      paddingLeft: 8,
                    }}
                  >
                    {up ? "+" : ""}
                    {d.reaction}%
                  </Text>
                </View>
                <Text style={[styles.historyDateLabel, { color: colors.textFaint }]}>{d.date}</Text>
              </View>
            </View>
          );
        })}
      </View>
      <View style={[styles.insightArea, { borderTopColor: colors.divider, backgroundColor: colors.surface }]}>
        <Text style={{ fontSize: 12, color: colors.textTertiary, lineHeight: 18 }}>
          💡 {item.insight}
        </Text>
      </View>
    </View>
  );
}

function AnomalyCard({ item }: { item: AnomalyItem }) {
  const { colors, isDark } = useTheme();
  const stock = STOCKS[item.stockCode];
  const maxVal = Math.max(...item.trend);

  const cardBg = isDark ? "#1C1416" : "#FFFBFB";
  const cardBorder = isDark ? "#3D2020" : "#FEE2E2";
  const badgeBg = isDark ? "#3D2020" : "#FEE2E2";
  const badgeColor = isDark ? "#F87171" : "#DC2626";

  return (
    <View style={[styles.anomalyCard, { backgroundColor: cardBg, borderColor: cardBorder }]}>
      <View style={{ padding: 14, paddingBottom: 10 }}>
        <View style={{ flexDirection: "row", alignItems: "center", gap: 6, marginBottom: 6 }}>
          <Text style={{ fontSize: 14 }}>{item.emoji}</Text>
          <View style={{ backgroundColor: badgeBg, paddingHorizontal: 7, paddingVertical: 2, borderRadius: 4 }}>
            <Text style={{ fontSize: 11, fontWeight: "600", color: badgeColor }}>이상 신호</Text>
          </View>
          <Text style={{ fontSize: 12, fontWeight: "600", color: colors.text }}>{stock.name}</Text>
        </View>
        <Text style={{ fontSize: 14, fontWeight: "700", color: colors.text, marginBottom: 4 }}>
          {item.title}
        </Text>
        <Text style={{ fontSize: 20, fontWeight: "800", color: "#DC2626", letterSpacing: -0.5 }}>
          {item.stat}
        </Text>
      </View>
      <View style={styles.trendContainer}>
        {item.trend.map((v, i) => (
          <View
            key={i}
            style={{
              flex: 1,
              height: `${(v / maxVal) * 100}%`,
              backgroundColor:
                i === item.trend.length - 1
                  ? "#EF4444"
                  : i >= item.trend.length - 3
                    ? "#FCA5A5"
                    : colors.track,
              borderRadius: 2,
              minHeight: 3,
            }}
          />
        ))}
      </View>
      <View style={[styles.anomalyDetailArea, { borderTopColor: cardBorder }]}>
        <Text style={{ fontSize: 12, color: colors.textMuted }}>{item.detail}</Text>
      </View>
    </View>
  );
}

function ConnectionCard({ item }: { item: ConnectionItem }) {
  const { colors, isDark } = useTheme();
  const stock1 = STOCKS[item.pair[0]];
  const stock2 = STOCKS[item.pair[1]];

  const cardBg = isDark ? "#14151F" : "#F8F9FF";
  const cardBorder = isDark ? "#2D2D4A" : "#E0E7FF";
  const badgeBg = isDark ? "#2D2D4A" : "#E0E7FF";
  const badgeColor = isDark ? "#A5B4FC" : "#3730A3";
  const stockBoxBorder = isDark ? "#4338CA" : "#C7D2FE";
  const lineColor = isDark ? "#6366F1" : "#A5B4FC";
  const countColor = isDark ? "#818CF8" : "#4F46E5";
  const keywordColor = isDark ? "#818CF8" : "#6366F1";
  const keywordBg = isDark ? "#1E1B4B" : "#EEF2FF";

  return (
    <View style={[styles.connectionCard, { backgroundColor: cardBg, borderColor: cardBorder }]}>
      <View style={{ flexDirection: "row", alignItems: "center", gap: 6, marginBottom: 10 }}>
        <Text style={{ fontSize: 14 }}>🔗</Text>
        <View style={{ backgroundColor: badgeBg, paddingHorizontal: 7, paddingVertical: 2, borderRadius: 4 }}>
          <Text style={{ fontSize: 11, fontWeight: "600", color: badgeColor }}>종목 연결</Text>
        </View>
      </View>
      <View style={styles.connectionPairRow}>
        <View style={[styles.connectionStockBox, { backgroundColor: colors.card, borderColor: stockBoxBorder }]}>
          <Text style={{ fontSize: 13, fontWeight: "700", color: colors.text }}>{stock1.name}</Text>
          <Text style={{ fontSize: 10, color: colors.textMuted, marginTop: 1 }}>{stock1.sector}</Text>
        </View>
        <View style={styles.connectionLineContainer}>
          <View style={[styles.connectionLine, { backgroundColor: lineColor }]} />
          <Text style={[styles.connectionCount, { color: countColor, backgroundColor: cardBg }]}>
            {item.count}회
          </Text>
        </View>
        <View style={[styles.connectionStockBox, { backgroundColor: colors.card, borderColor: stockBoxBorder }]}>
          <Text style={{ fontSize: 13, fontWeight: "700", color: colors.text }}>{stock2.name}</Text>
          <Text style={{ fontSize: 10, color: colors.textMuted, marginTop: 1 }}>{stock2.sector}</Text>
        </View>
      </View>
      <Text style={{ fontSize: 12, color: colors.textTertiary, marginBottom: 8, textAlign: "center" }}>
        {item.period} 동시 언급 {item.count}회
      </Text>
      <View style={styles.keywordRow}>
        {item.topKeywords.map((k) => (
          <View key={k} style={[styles.keywordChip, { backgroundColor: keywordBg }]}>
            <Text style={{ fontSize: 10, color: keywordColor }}>#{k}</Text>
          </View>
        ))}
      </View>
    </View>
  );
}

function StatCard({ item }: { item: StatItem }) {
  const { colors } = useTheme();
  const medals = ["🥇", "🥈", "🥉"];

  return (
    <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
      <View style={{ padding: 14 }}>
        <View style={{ flexDirection: "row", alignItems: "center", gap: 6, marginBottom: 12 }}>
          <Text style={{ fontSize: 16 }}>{item.emoji}</Text>
          <Text style={{ fontSize: 13, fontWeight: "700", color: colors.text }}>{item.title}</Text>
        </View>
        {item.items.map((s, i) => {
          const stock = STOCKS[s.code];
          return (
            <View
              key={s.code}
              style={[
                styles.statRow,
                i < item.items.length - 1 && { borderBottomWidth: 1, borderBottomColor: colors.divider },
              ]}
            >
              <Text style={{ fontSize: i === 0 ? 18 : 14, width: 28, textAlign: "center" }}>
                {medals[i]}
              </Text>
              <Text style={{ fontSize: 13, fontWeight: "600", color: colors.text, flex: 1 }}>
                {stock.name}
              </Text>
              <Text style={{ fontSize: 13, fontWeight: "700", color: colors.textTertiary }}>
                {s.newsCount}건
              </Text>
            </View>
          );
        })}
      </View>
    </View>
  );
}

// ─── Feed Screen ───

function renderItem({ item }: { item: FeedItem }) {
  switch (item.type) {
    case "session_divider":
      return <SessionDivider item={item} />;
    case "event":
      return <EventCard item={item} />;
    case "sector_mood":
      return <SectorMoodCard item={item} />;
    case "history":
      return <HistoryCard item={item} />;
    case "anomaly":
      return <AnomalyCard item={item} />;
    case "connection":
      return <ConnectionCard item={item} />;
    case "stat":
      return <StatCard item={item} />;
    default:
      return null;
  }
}

export default function FeedScreen() {
  const { colors } = useTheme();

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
      <View style={[styles.header, { borderBottomColor: colors.divider }]}>
        <Text style={[styles.headerTitle, { color: colors.text }]}>피드</Text>
        <Text style={[styles.headerSub, { color: colors.textFaint }]}>5종목 구독중</Text>
      </View>
      <FlatList
        data={FEED}
        renderItem={renderItem}
        keyExtractor={(item) => item.id}
        contentContainerStyle={{ paddingTop: 8, paddingBottom: 40 }}
      />
    </SafeAreaView>
  );
}

// ─── Styles (layout only, colors applied inline) ───

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    paddingHorizontal: 20,
    paddingTop: 12,
    paddingBottom: 10,
    borderBottomWidth: 1,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: "800",
    letterSpacing: -0.7,
  },
  headerSub: {
    fontSize: 12,
  },

  // Session Divider
  sessionDivider: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 24,
    paddingVertical: 6,
    gap: 8,
    marginVertical: 4,
  },
  sessionLine: {
    flex: 1,
    height: 1,
  },
  sessionLabel: {
    flexDirection: "row",
    alignItems: "center",
    gap: 5,
    paddingHorizontal: 10,
    paddingVertical: 3,
    borderRadius: 10,
  },
  sessionDot: {
    width: 5,
    height: 5,
    borderRadius: 2.5,
  },
  sessionText: {
    fontSize: 11,
    fontWeight: "600",
    letterSpacing: 0.1,
  },
  sessionTimeRange: {
    fontSize: 10,
  },

  // Card base
  card: {
    marginHorizontal: 16,
    marginBottom: 12,
    borderRadius: 14,
    borderWidth: 1,
    overflow: "hidden",
  },

  // Event Card
  eventImageContainer: {
    height: 150,
    overflow: "hidden",
  },
  decoCircle: {
    position: "absolute",
    backgroundColor: "white",
  },
  eventImageOverlay: {
    position: "absolute",
    bottom: 0,
    left: 0,
    right: 0,
    height: 60,
  },
  eventImageBadges: {
    position: "absolute",
    bottom: 10,
    left: 12,
    flexDirection: "row",
    gap: 6,
    alignItems: "center",
  },
  eventTypeBadgeOnImage: {
    backgroundColor: "rgba(255,255,255,0.2)",
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 5,
  },
  eventArticleCount: {
    position: "absolute",
    top: 10,
    right: 12,
    backgroundColor: "rgba(0,0,0,0.3)",
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
  },
  eventHeadline: {
    fontSize: 15.5,
    fontWeight: "700",
    lineHeight: 22,
    letterSpacing: -0.3,
    marginBottom: 6,
  },
  eventSummary: {
    fontSize: 13,
    lineHeight: 19.5,
    marginBottom: 12,
  },
  stockReactionArea: {
    borderTopWidth: 1,
    paddingHorizontal: 14,
    paddingVertical: 10,
  },
  stockReactionRow: {
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 3,
    gap: 8,
  },
  stockName: {
    fontSize: 13,
    fontWeight: "600",
    width: 72,
  },
  stockRole: {
    fontSize: 10,
    width: 52,
  },
  reactionBarTrack: {
    flex: 1,
    height: 4,
    borderRadius: 2,
    overflow: "hidden",
  },
  reactionBarFill: {
    height: "100%",
    borderRadius: 2,
  },
  reactionPercent: {
    fontSize: 12,
    fontWeight: "700",
    width: 48,
    textAlign: "right",
  },

  // Sector Mood
  sectorMoodCard: {
    marginHorizontal: 16,
    marginBottom: 12,
    borderRadius: 14,
    padding: 14,
    paddingHorizontal: 16,
    borderWidth: 1,
  },
  sectorStockBox: {
    flex: 1,
    padding: 8,
    paddingHorizontal: 10,
    borderRadius: 8,
    borderWidth: 1,
  },

  // History
  historyBarTrack: {
    flex: 1,
    height: 22,
    borderRadius: 6,
    overflow: "hidden",
    justifyContent: "center",
  },
  historyBarFill: {
    height: "100%",
    borderRadius: 6,
    justifyContent: "center",
  },
  historyDateLabel: {
    position: "absolute",
    right: 8,
    fontSize: 10,
    alignSelf: "center",
  },
  insightArea: {
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderTopWidth: 1,
  },

  // Anomaly
  anomalyCard: {
    marginHorizontal: 16,
    marginBottom: 12,
    borderRadius: 14,
    borderWidth: 1,
    overflow: "hidden",
  },
  trendContainer: {
    paddingHorizontal: 14,
    paddingBottom: 8,
    height: 40,
    flexDirection: "row",
    alignItems: "flex-end",
    gap: 3,
  },
  anomalyDetailArea: {
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderTopWidth: 1,
  },

  // Connection
  connectionCard: {
    marginHorizontal: 16,
    marginBottom: 12,
    borderRadius: 14,
    borderWidth: 1,
    overflow: "hidden",
    padding: 14,
  },
  connectionPairRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 4,
    paddingBottom: 10,
  },
  connectionStockBox: {
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 10,
    borderWidth: 1.5,
    alignItems: "center",
  },
  connectionLineContainer: {
    width: 50,
    alignItems: "center",
    justifyContent: "center",
  },
  connectionLine: {
    width: "100%",
    height: 2,
  },
  connectionCount: {
    position: "absolute",
    top: -9,
    fontSize: 10,
    fontWeight: "700",
    paddingHorizontal: 4,
  },
  keywordRow: {
    flexDirection: "row",
    gap: 4,
    flexWrap: "wrap",
    justifyContent: "center",
  },
  keywordChip: {
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 10,
  },

  // Stat
  statRow: {
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 6,
    gap: 10,
  },

  // Comment
  commentRow: {
    flexDirection: "row",
    gap: 8,
    paddingVertical: 8,
  },
  commentAvatar: {
    width: 28,
    height: 28,
    borderRadius: 14,
    alignItems: "center",
    justifyContent: "center",
  },
  commentAvatarText: {
    fontSize: 12,
    fontWeight: "700",
  },

  // Reaction button
  reactionButton: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4,
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 16,
    borderWidth: 1.5,
  },
});
