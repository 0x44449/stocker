import { useEffect, useState } from "react";
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  ActivityIndicator,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { LinearGradient } from "expo-linear-gradient";
import { useTheme } from "../../src/theme";

const API_BASE_URL = "http://localhost:28080";

const STOCK_NAMES = [
  "삼성전자",
  "SK하이닉스",
  "NAVER",
  "카카오",
  "현대차",
  "LG에너지솔루션",
  "삼성바이오로직스",
  "셀트리온",
  "KB금융",
  "신한지주",
];

// --- API 응답 타입 (StockTopicsDto 구조) ---

interface ArticleDto {
  news_id: number;
  title: string;
}

interface StockPriceDto {
  stock_code: string;
  date: string;
  close: number | null;
  diff_rate: number | null;
}

interface RelatedStockDto {
  stock_name: string;
  stock_code: string;
  mention_count: number;
  close: number | null;
  diff_rate: number | null;
}

interface TopicDto {
  title: string;
  summary: string;
  count: number;
  articles: ArticleDto[];
}

interface ClusterDto {
  count: number;
  articles: ArticleDto[];
}

interface StockTopicsDto {
  keyword: string;
  total_count: number;
  stock_price: StockPriceDto | null;
  related_stock: RelatedStockDto | null;
  topic: TopicDto | null;
  clusters: ClusterDto[];
  noise: ArticleDto[];
}

// --- TimeDisplay 컴포넌트 (하드코딩) ---

function TimeDisplay() {
  const { colors, isDark } = useTheme();
  const sessionColor = isDark ? "#9CA3AF" : "#6B7280";

  return (
    <View style={{ flexDirection: "row", alignItems: "center", gap: 4 }}>
      <Text style={{ fontSize: 9, fontWeight: "700", color: sessionColor, letterSpacing: 0.2 }}>
        장후
      </Text>
      <Text style={{ fontSize: 11, color: colors.textFaint }}>16:02</Text>
    </View>
  );
}

// --- 카드 컴포넌트 ---

function StockTopicCard({ data }: { data: StockTopicsDto }) {
  const { colors, isDark } = useTheme();

  const headline = data.topic?.title ?? `${data.keyword} 관련 뉴스`;
  const summary = data.topic?.summary;

  // 주체 종목 변동률
  const mainRate = data.stock_price?.diff_rate ?? null;
  const mainUp = mainRate !== null && mainRate > 0;

  // 연관 종목 변동률
  const relatedRate = data.related_stock?.diff_rate ?? null;
  const relatedUp = relatedRate !== null && relatedRate > 0;

  return (
    <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
      {/* 그라데이션 이미지 영역 */}
      <View style={[styles.eventImageContainer, { backgroundColor: colors.track }]}>
        <LinearGradient
          colors={["#1a1a2e", "#16213e", "#0f3460", "#533483"]}
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
        {/* 좌하단: 타입 배지 + 시간 */}
        <View style={styles.eventImageBadges}>
          <View style={styles.eventTypeBadgeOnImage}>
            <Text style={{ fontSize: 11, fontWeight: "600", color: "#FFF" }}>
              📊 실적
            </Text>
          </View>
          <TimeDisplay />
        </View>
        {/* 우상단: 기사 건수 */}
        <View style={styles.eventArticleCount}>
          <Text style={{ fontSize: 10, color: "rgba(255,255,255,0.5)" }}>
            기사 {data.total_count}건
          </Text>
        </View>
      </View>

      {/* 헤드라인 + 요약 */}
      <View style={{ padding: 12, paddingHorizontal: 14, paddingBottom: 0 }}>
        <Text style={[styles.eventHeadline, { color: colors.text }]}>{headline}</Text>
        {summary && (
          <Text style={[styles.eventSummary, { color: colors.textMuted }]}>{summary}</Text>
        )}
      </View>

      {/* 주가 반응 영역 */}
      <View style={[styles.stockReactionArea, { borderTopColor: colors.divider, backgroundColor: colors.surface }]}>
        {/* 주체 종목 */}
        <View style={styles.stockReactionRow}>
          <Text style={[styles.stockName, { color: colors.text }]}>{data.keyword}</Text>
          <Text style={[styles.stockRole, { color: colors.textFaint }]}>주체</Text>
          <View style={{ flex: 1, flexDirection: "row", alignItems: "center", gap: 6 }}>
            <View style={[styles.reactionBarTrack, { backgroundColor: colors.track }]}>
              {mainRate !== null && (
                <View
                  style={[
                    styles.reactionBarFill,
                    {
                      width: `${Math.min(Math.abs(mainRate) * 5, 50)}%`,
                      backgroundColor: mainUp ? "#EF4444" : "#3B82F6",
                      left: mainUp ? "50%" : undefined,
                      right: mainUp ? undefined : "50%",
                    },
                  ]}
                />
              )}
              <View style={[styles.reactionBarCenter, { backgroundColor: colors.textFaint }]} />
            </View>
            <Text
              style={[
                styles.reactionPercent,
                { color: mainRate === null ? "#999" : mainUp ? "#DC2626" : "#2563EB" },
              ]}
            >
              {mainRate !== null ? `${mainUp ? "+" : ""}${mainRate.toFixed(2)}%` : "-"}
            </Text>
          </View>
        </View>

        {/* 연관 종목 */}
        {data.related_stock && (
          <View style={[styles.stockReactionRow, { opacity: 0.55 }]}>
            <Text style={[styles.stockName, { color: colors.text }]}>{data.related_stock.stock_name}</Text>
            <Text style={[styles.stockRole, { color: colors.textFaint }]}>연관종목</Text>
            <View style={{ flex: 1, flexDirection: "row", alignItems: "center", gap: 6 }}>
              <View style={[styles.reactionBarTrack, { backgroundColor: colors.track }]}>
                {relatedRate !== null && (
                  <View
                    style={[
                      styles.reactionBarFill,
                      {
                        width: `${Math.min(Math.abs(relatedRate) * 5, 50)}%`,
                        backgroundColor: relatedUp ? "#EF4444" : "#3B82F6",
                        left: relatedUp ? "50%" : undefined,
                        right: relatedUp ? undefined : "50%",
                      },
                    ]}
                  />
                )}
                <View style={[styles.reactionBarCenter, { backgroundColor: colors.textFaint }]} />
              </View>
              <Text
                style={[
                  styles.reactionPercent,
                  { color: relatedRate === null ? "#999" : relatedUp ? "#DC2626" : "#2563EB" },
                ]}
              >
                {relatedRate !== null ? `${relatedUp ? "+" : ""}${relatedRate.toFixed(2)}%` : "-"}
              </Text>
            </View>
          </View>
        )}
      </View>
    </View>
  );
}

// --- 피드 화면 ---

export default function FeedTab() {
  const { colors } = useTheme();
  const [data, setData] = useState<StockTopicsDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAll();
  }, []);

  async function fetchAll() {
    setLoading(true);
    const results = await Promise.allSettled(
      STOCK_NAMES.map((name) =>
        fetch(`${API_BASE_URL}/api/feed/stock-topics?keyword=${encodeURIComponent(name)}&days=2&eps=0.2`)
          .then((res) => res.json() as Promise<StockTopicsDto>)
      )
    );

    const items = results
      .filter((r): r is PromiseFulfilledResult<StockTopicsDto> => r.status === "fulfilled")
      .map((r) => r.value)
      .filter((d) => d.total_count > 0);

    setData(items);
    setLoading(false);
  }

  if (loading) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
        <View style={[styles.header, { borderBottomColor: colors.divider }]}>
          <Text style={[styles.headerTitle, { color: colors.text }]}>피드</Text>
          <Text style={[styles.headerSub, { color: colors.textFaint }]}>10종목</Text>
        </View>
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color={colors.textMuted} />
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
      <View style={[styles.header, { borderBottomColor: colors.divider }]}>
        <Text style={[styles.headerTitle, { color: colors.text }]}>피드</Text>
        <Text style={[styles.headerSub, { color: colors.textFaint }]}>10종목</Text>
      </View>
      {data.length === 0 ? (
        <View style={styles.emptyContainer}>
          <Text style={{ color: colors.textMuted, fontSize: 14 }}>데이터 없음</Text>
        </View>
      ) : (
        <FlatList
          data={data}
          renderItem={({ item }) => <StockTopicCard data={item} />}
          keyExtractor={(item) => item.keyword}
          contentContainerStyle={{ paddingTop: 8, paddingBottom: 40 }}
        />
      )}
    </SafeAreaView>
  );
}

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
  loadingContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
  emptyContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },

  // 카드
  card: {
    marginHorizontal: 16,
    marginBottom: 12,
    borderRadius: 14,
    borderWidth: 1,
    overflow: "hidden",
  },

  // 이미지 영역
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

  // 헤드라인 / 요약
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

  // 주가 반응 영역
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
    position: "relative",
  },
  reactionBarFill: {
    position: "absolute",
    height: "100%",
    borderRadius: 2,
  },
  reactionBarCenter: {
    position: "absolute",
    left: "50%",
    width: 1,
    height: "100%",
    opacity: 0.3,
  },
  reactionPercent: {
    fontSize: 12,
    fontWeight: "700",
    width: 48,
    textAlign: "right",
  },
});
