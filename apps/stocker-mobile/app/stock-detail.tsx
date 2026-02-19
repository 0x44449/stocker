import { useEffect, useState } from "react";
import { View, Text, FlatList, StyleSheet, TouchableOpacity, ActivityIndicator } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams, useRouter } from "expo-router";
import { useTheme } from "../src/theme";
import { useAuth } from "../lib/auth";
import { API_BASE_URL } from "../lib/config";

// --- API 응답 타입 ---

interface StockPriceDto {
  close: number | null;
  diff: number | null;
  diff_rate: number | null;
}

interface TopicDto {
  title: string;
  summary: string;
  count: number;
  time: string | null;
}

interface ClusterDto {
  title: string | null;
  count: number;
  time: string | null;
}

interface StockTopicsDto {
  stock_code: string;
  stock_name: string;
  total_count: number;
  stock_price: StockPriceDto | null;
  topic: TopicDto | null;
  clusters: ClusterDto[];
}

// --- 타임라인 표시용 ---

interface TimelineEntry {
  headline: string;
  summary: string | null;
  time: string | null;
  articleCount: number;
}

// --- 포맷 헬퍼 ---

function formatPrice(price: number): string {
  return price.toLocaleString("ko-KR");
}

function formatChangeRate(rate: number): string {
  const sign = rate > 0 ? "+" : "";
  return `${sign}${rate.toFixed(2)}%`;
}

function formatChangeAmount(amount: number): string {
  const sign = amount > 0 ? "+" : "";
  return `${sign}${amount.toLocaleString("ko-KR")}`;
}

// --- 타임라인 항목 ---

function TimelineItem({ entry, colors }: {
  entry: TimelineEntry;
  colors: any;
}) {
  return (
    <View style={styles.timelineItem}>
      {/* 왼쪽 타임라인 도트 + 선 */}
      <View style={styles.timelineLeft}>
        <View style={[styles.timelineDot, { backgroundColor: colors.textMuted }]} />
        <View style={[styles.timelineLine, { backgroundColor: colors.divider }]} />
      </View>

      {/* 오른쪽 콘텐츠 */}
      <View style={styles.timelineContent}>
        {entry.time && (
          <View style={styles.timelineTopRow}>
            <Text style={[styles.timeText, { color: colors.textFaint }]}>{entry.time}</Text>
            <Text style={[styles.articleCount, { color: colors.textMuted }]}>
              기사 {entry.articleCount}건
            </Text>
          </View>
        )}
        <Text style={[styles.timelineHeadline, { color: colors.text }]}>{entry.headline}</Text>
        {entry.summary && (
          <Text style={[styles.timelineSummary, { color: colors.textMuted }]} numberOfLines={3}>
            {entry.summary}
          </Text>
        )}
      </View>
    </View>
  );
}

export default function StockDetailScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const { session } = useAuth();
  const { stockCode } = useLocalSearchParams<{ stockCode: string }>();
  const [data, setData] = useState<StockTopicsDto | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!stockCode) return;

    async function fetchData() {
      const headers: Record<string, string> = {};
      if (session?.access_token) {
        headers["Authorization"] = `Bearer ${session.access_token}`;
      }
      try {
        const res = await fetch(
          `${API_BASE_URL}/api/feed/stock-topics?stockCode=${stockCode}`,
          { headers }
        );
        if (res.ok) {
          setData(await res.json());
        }
      } finally {
        setLoading(false);
      }
    }

    fetchData();
  }, [stockCode]);

  if (loading) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
        <TouchableOpacity style={styles.backButton} onPress={() => router.back()}>
          <Text style={[styles.backButtonText, { color: colors.text }]}>{"←"}</Text>
        </TouchableOpacity>
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color={colors.textMuted} />
        </View>
      </SafeAreaView>
    );
  }

  if (!data) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
        <TouchableOpacity style={styles.backButton} onPress={() => router.back()}>
          <Text style={[styles.backButtonText, { color: colors.text }]}>{"←"}</Text>
        </TouchableOpacity>
        <View style={styles.emptyContainer}>
          <Text style={{ color: colors.textMuted, fontSize: 13 }}>종목을 찾을 수 없습니다</Text>
        </View>
      </SafeAreaView>
    );
  }

  const price = data.stock_price;
  const isUp = (price?.diff_rate ?? 0) > 0;
  const rateColor = price?.diff_rate === 0 || price?.diff_rate == null ? colors.textMuted : isUp ? "#DC2626" : "#2563EB";

  // 타임라인 엔트리 구성 (topic + clusters)
  const entries: TimelineEntry[] = [];
  if (data.topic) {
    entries.push({
      headline: data.topic.title,
      summary: data.topic.summary,
      time: data.topic.time,
      articleCount: data.topic.count,
    });
  }
  for (const c of data.clusters) {
    entries.push({
      headline: c.title ?? `관련 뉴스 ${c.count}건`,
      summary: null,
      time: c.time,
      articleCount: c.count,
    });
  }

  const stockHeader = (
    <View style={styles.stockHeader}>
      <View style={styles.stockHeaderTop}>
        <View>
          <Text style={[styles.stockName, { color: colors.text }]}>{data.stock_name ?? stockCode}</Text>
          <Text style={[styles.stockCode, { color: colors.textFaint }]}>{data.stock_code}</Text>
        </View>
        <View style={styles.stockHeaderRight}>
          {price?.close != null ? (
            <>
              <Text style={[styles.price, { color: colors.text }]}>{formatPrice(price.close)}</Text>
              <View style={styles.changeRow}>
                {price.diff_rate != null && (
                  <View style={[styles.rateBadge, { backgroundColor: isUp ? "#FEE2E2" : "#DBEAFE" }]}>
                    <Text style={[styles.rateBadgeText, { color: rateColor }]}>
                      {formatChangeRate(price.diff_rate)}
                    </Text>
                  </View>
                )}
                {price.diff != null && (
                  <Text style={[styles.changeAmount, { color: rateColor }]}>
                    {formatChangeAmount(price.diff)}
                  </Text>
                )}
              </View>
            </>
          ) : (
            <Text style={{ color: colors.textMuted, fontSize: 13 }}>-</Text>
          )}
        </View>
      </View>
    </View>
  );

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
      <TouchableOpacity style={styles.backButton} onPress={() => router.back()}>
        <Text style={[styles.backButtonText, { color: colors.text }]}>{"←"}</Text>
      </TouchableOpacity>

      <FlatList
        data={entries}
        renderItem={({ item }) => <TimelineItem entry={item} colors={colors} />}
        keyExtractor={(_, i) => String(i)}
        ListHeaderComponent={stockHeader}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={{ color: colors.textMuted, fontSize: 13 }}>최근 주요 뉴스가 없습니다</Text>
          </View>
        }
        contentContainerStyle={{ paddingBottom: 40 }}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
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

  // 뒤로가기
  backButton: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    alignSelf: "flex-start",
  },
  backButtonText: {
    fontSize: 24,
  },

  // 종목 헤더
  stockHeader: {
    paddingHorizontal: 20,
    paddingTop: 4,
    paddingBottom: 14,
  },
  stockHeaderTop: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "flex-start",
  },
  stockHeaderRight: {
    alignItems: "flex-end",
  },
  stockName: {
    fontSize: 22,
    fontWeight: "800",
    letterSpacing: -0.7,
  },
  stockCode: {
    fontSize: 12,
    marginTop: 2,
  },
  price: {
    fontSize: 20,
    fontWeight: "700",
  },
  changeRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    marginTop: 4,
  },
  rateBadge: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
  },
  rateBadgeText: {
    fontSize: 13,
    fontWeight: "700",
  },
  changeAmount: {
    fontSize: 13,
    fontWeight: "500",
  },

  // 타임라인
  timelineItem: {
    flexDirection: "row",
    paddingHorizontal: 16,
    marginBottom: 0,
  },
  timelineLeft: {
    width: 24,
    alignItems: "center",
  },
  timelineDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    marginTop: 14,
  },
  timelineLine: {
    width: 2,
    flex: 1,
    marginTop: 4,
  },
  timelineContent: {
    flex: 1,
    marginLeft: 8,
    marginBottom: 12,
    padding: 12,
  },
  timelineTopRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    marginBottom: 6,
  },
  timeText: {
    fontSize: 10,
  },
  timelineHeadline: {
    fontSize: 13,
    fontWeight: "500",
    lineHeight: 18,
    letterSpacing: -0.2,
    marginBottom: 6,
  },
  timelineSummary: {
    fontSize: 12,
    lineHeight: 17,
  },
  articleCount: {
    fontSize: 10,
  },
});
