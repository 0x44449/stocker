import { useEffect, useState } from "react";
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  ActivityIndicator,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
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

function formatPrice(price: number | null): string {
  if (price == null) return "-";
  return price.toLocaleString() + "원";
}

function formatRate(rate: number | null): string {
  if (rate == null) return "";
  const sign = rate > 0 ? "+" : "";
  return `${sign}${rate.toFixed(2)}%`;
}

function rateColor(rate: number | null): string {
  if (rate == null) return "#999";
  if (rate > 0) return "#DC2626";
  if (rate < 0) return "#2563EB";
  return "#999";
}

// --- 카드 컴포넌트 ---

function StockTopicCard({ data }: { data: StockTopicsDto }) {
  const { colors, isDark } = useTheme();

  return (
    <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
      {/* 종목 헤더 */}
      <View style={styles.cardHeader}>
        <Text style={[styles.stockName, { color: colors.text }]}>{data.keyword}</Text>
        <View style={styles.priceArea}>
          <Text style={[styles.priceText, { color: colors.text }]}>
            {formatPrice(data.stock_price?.close ?? null)}
          </Text>
          <Text style={[styles.rateText, { color: rateColor(data.stock_price?.diff_rate ?? null) }]}>
            {formatRate(data.stock_price?.diff_rate ?? null)}
          </Text>
        </View>
      </View>

      {/* 토픽 */}
      {data.topic && (
        <View style={[styles.topicSection, { borderTopColor: colors.divider }]}>
          <Text style={[styles.topicTitle, { color: colors.text }]}>
            📰 {data.topic.title} ({data.topic.count}건)
          </Text>
          <Text style={[styles.topicSummary, { color: colors.textSecondary }]}>
            {data.topic.summary}
          </Text>
        </View>
      )}

      {/* 관련 종목 */}
      {data.related_stock && (
        <View style={[styles.relatedSection, { borderTopColor: colors.divider, backgroundColor: colors.surface }]}>
          <Text style={[styles.relatedLabel, { color: colors.textMuted }]}>
            관련: {data.related_stock.stock_name} ({data.related_stock.mention_count}회 언급)
          </Text>
          <View style={styles.relatedPriceRow}>
            <Text style={[styles.relatedPrice, { color: colors.textSecondary }]}>
              {formatPrice(data.related_stock.close)}
            </Text>
            <Text style={[styles.relatedRate, { color: rateColor(data.related_stock.diff_rate) }]}>
              {formatRate(data.related_stock.diff_rate)}
            </Text>
          </View>
        </View>
      )}
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
  cardHeader: {
    padding: 14,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "flex-start",
  },
  stockName: {
    fontSize: 16,
    fontWeight: "700",
  },
  priceArea: {
    alignItems: "flex-end",
  },
  priceText: {
    fontSize: 15,
    fontWeight: "700",
  },
  rateText: {
    fontSize: 13,
    fontWeight: "600",
    marginTop: 2,
  },

  // 토픽
  topicSection: {
    padding: 14,
    paddingTop: 12,
    borderTopWidth: 1,
  },
  topicTitle: {
    fontSize: 14,
    fontWeight: "700",
    marginBottom: 6,
  },
  topicSummary: {
    fontSize: 13,
    lineHeight: 19,
  },

  // 관련 종목
  relatedSection: {
    padding: 14,
    paddingVertical: 10,
    borderTopWidth: 1,
  },
  relatedLabel: {
    fontSize: 12,
    marginBottom: 4,
  },
  relatedPriceRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  relatedPrice: {
    fontSize: 13,
    fontWeight: "600",
  },
  relatedRate: {
    fontSize: 13,
    fontWeight: "600",
  },
});
