import { View, Text, FlatList, StyleSheet, TouchableOpacity } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams, useRouter } from "expo-router";
import { useTheme } from "../src/theme";
import { findStockByCode, ClusterItem } from "../src/mock/watchlistMock";

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

// 세션 라벨
function getSessionLabel(session: string, isDark: boolean): { text: string; color: string } {
  const labels: Record<string, { text: string; light: string; dark: string }> = {
    pre: { text: "장전", light: "#D97706", dark: "#FBBF24" },
    open: { text: "장중", light: "#059669", dark: "#34D399" },
    post: { text: "장후", light: "#6B7280", dark: "#9CA3AF" },
  };
  const l = labels[session] ?? labels["open"];
  return { text: l.text, color: isDark ? l.dark : l.light };
}

// 타임라인 항목
function TimelineItem({ cluster, isDark, colors, onPress }: {
  cluster: ClusterItem;
  isDark: boolean;
  colors: any;
  onPress: () => void;
}) {
  const sessionLabel = getSessionLabel(cluster.session, isDark);

  return (
    <TouchableOpacity style={styles.timelineItem} onPress={onPress} activeOpacity={0.6}>
      {/* 왼쪽 타임라인 도트 + 선 */}
      <View style={styles.timelineLeft}>
        <View style={[styles.timelineDot, { backgroundColor: colors.textMuted }]} />
        <View style={[styles.timelineLine, { backgroundColor: colors.divider }]} />
      </View>

      {/* 오른쪽 콘텐츠 */}
      <View style={styles.timelineContent}>
        <View style={styles.timelineTopRow}>
          <Text style={[styles.sessionText, { color: sessionLabel.color }]}>{sessionLabel.text}</Text>
          <Text style={[styles.timeText, { color: colors.textFaint }]}>{cluster.time}</Text>
        </View>
        <Text style={[styles.timelineHeadline, { color: colors.text }]}>{cluster.headline}</Text>
        <Text style={[styles.articleCount, { color: colors.textMuted }]}>
          기사 {cluster.articleCount}건
        </Text>
      </View>
    </TouchableOpacity>
  );
}

export default function StockDetailScreen() {
  const { colors, isDark } = useTheme();
  const router = useRouter();
  const { stockCode } = useLocalSearchParams<{ stockCode: string }>();

  const stock = findStockByCode(stockCode ?? "");

  if (!stock) {
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

  const isUp = stock.changeRate > 0;
  const rateColor = stock.changeRate === 0 ? colors.textMuted : isUp ? "#DC2626" : "#2563EB";

  const sortedClusters = [...stock.clusters];

  const goToArticles = (clusterId: string) => {
    router.push({ pathname: "/article-list", params: { clusterId } });
  };

  const stockHeader = (
    <View style={styles.stockHeader}>
      <View style={styles.stockHeaderTop}>
        <View>
          <Text style={[styles.stockName, { color: colors.text }]}>{stock.stockName}</Text>
          <Text style={[styles.stockCode, { color: colors.textFaint }]}>{stock.stockCode}</Text>
        </View>
        <View style={styles.stockHeaderRight}>
          <Text style={[styles.price, { color: colors.text }]}>{formatPrice(stock.price)}</Text>
          <View style={styles.changeRow}>
            <View style={[styles.rateBadge, { backgroundColor: isUp ? "#FEE2E2" : "#DBEAFE" }]}>
              <Text style={[styles.rateBadgeText, { color: rateColor }]}>
                {formatChangeRate(stock.changeRate)}
              </Text>
            </View>
            <Text style={[styles.changeAmount, { color: rateColor }]}>
              {formatChangeAmount(stock.changeAmount)}
            </Text>
          </View>
        </View>
      </View>
    </View>
  );

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
      {/* 고정 헤더: 뒤로가기 */}
      <TouchableOpacity style={styles.backButton} onPress={() => router.back()}>
        <Text style={[styles.backButtonText, { color: colors.text }]}>{"←"}</Text>
      </TouchableOpacity>

      <FlatList
        data={sortedClusters}
        renderItem={({ item }) => (
          <TimelineItem
            cluster={item}
            isDark={isDark}
            colors={colors}
            onPress={() => goToArticles(item.clusterId)}
          />
        )}
        keyExtractor={(item) => item.clusterId}
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
  sessionText: {
    fontSize: 10,
    fontWeight: "600",
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
  articleCount: {
    fontSize: 10,
  },
});
