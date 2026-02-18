import { View, Text, FlatList, StyleSheet, TouchableOpacity } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import { useTheme } from "../../src/theme";
import { WATCHLIST_STOCKS, WatchlistStock, ClusterItem } from "../../src/mock/watchlistMock";

// 카테고리별 뱃지 스타일
const CATEGORY_STYLES: Record<string, { color: string; bg: string; darkColor: string; darkBg: string }> = {
  "실적": { color: "#92400E", bg: "#FEF3C7", darkColor: "#FCD34D", darkBg: "#42200680" },
  "사업": { color: "#1E40AF", bg: "#DBEAFE", darkColor: "#93C5FD", darkBg: "#1E3A5C80" },
  "규제": { color: "#991B1B", bg: "#FEE2E2", darkColor: "#FCA5A5", darkBg: "#3D202080" },
  "경영": { color: "#6B21A8", bg: "#F3E8FF", darkColor: "#C4B5FD", darkBg: "#3B1F5680" },
  "산업": { color: "#3730A3", bg: "#E0E7FF", darkColor: "#A5B4FC", darkBg: "#2D2D4A80" },
  "기술": { color: "#065F46", bg: "#D1FAE5", darkColor: "#6EE7B7", darkBg: "#064E3B80" },
};

function getCategoryStyle(category: string, isDark: boolean) {
  const style = CATEGORY_STYLES[category] ?? CATEGORY_STYLES["사업"];
  return isDark
    ? { color: style.darkColor, bg: style.darkBg }
    : { color: style.color, bg: style.bg };
}

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

// 클러스터 항목
function ClusterRow({ cluster, isDark, colors, onPress }: {
  cluster: ClusterItem;
  isDark: boolean;
  colors: any;
  onPress: () => void;
}) {
  const catStyle = getCategoryStyle(cluster.category, isDark);
  const isUp = cluster.changeRate > 0;
  const rateColor = cluster.changeRate === 0 ? colors.textMuted : isUp ? "#DC2626" : "#2563EB";

  return (
    <TouchableOpacity
      style={[styles.clusterRow, { borderTopColor: colors.divider }]}
      onPress={onPress}
      activeOpacity={0.6}
    >
      <View style={styles.clusterLeft}>
        <View style={[styles.categoryBadge, { backgroundColor: catStyle.bg }]}>
          <Text style={[styles.categoryBadgeText, { color: catStyle.color }]}>{cluster.category}</Text>
        </View>
        <Text style={[styles.clusterHeadline, { color: colors.text }]} numberOfLines={1}>
          {cluster.headline}
        </Text>
      </View>
      <View style={styles.clusterRight}>
        <View style={styles.clusterMeta}>
          <Text style={[styles.clusterTime, { color: colors.textFaint }]}>{cluster.time}</Text>
          <Text style={[styles.clusterArticleCount, { color: colors.textMuted }]}>
            {cluster.articleCount}건
          </Text>
        </View>
        <Text style={[styles.clusterRate, { color: rateColor }]}>
          {formatChangeRate(cluster.changeRate)}
        </Text>
      </View>
    </TouchableOpacity>
  );
}

// 종목 카드
function StockCard({ stock }: { stock: WatchlistStock }) {
  const { colors, isDark } = useTheme();
  const router = useRouter();
  const isUp = stock.changeRate > 0;
  const rateColor = stock.changeRate === 0 ? colors.textMuted : isUp ? "#DC2626" : "#2563EB";

  const goToDetail = () => {
    router.push({ pathname: "/stock-detail", params: { stockCode: stock.stockCode } });
  };

  const goToArticles = (clusterId: string) => {
    router.push({ pathname: "/article-list", params: { clusterId } });
  };

  return (
    <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
      {/* 카드 헤더 */}
      <TouchableOpacity style={styles.cardHeader} onPress={goToDetail} activeOpacity={0.6}>
        <View style={styles.cardHeaderLeft}>
          <Text style={[styles.stockName, { color: colors.text }]}>{stock.stockName}</Text>
          <Text style={[styles.stockCode, { color: colors.textFaint }]}>{stock.stockCode}</Text>
        </View>
        <View style={styles.cardHeaderRight}>
          <Text style={[styles.price, { color: colors.text }]}>{formatPrice(stock.price)}</Text>
          <View style={styles.changeRow}>
            <Text style={[styles.changeRate, { color: rateColor }]}>
              {formatChangeRate(stock.changeRate)}
            </Text>
            <Text style={[styles.changeAmount, { color: rateColor }]}>
              {formatChangeAmount(stock.changeAmount)}
            </Text>
          </View>
        </View>
        {stock.newsCount > 0 && (
          <View style={[styles.newsCountBadge, { backgroundColor: isDark ? "#3A3A3C" : "#F3F4F6" }]}>
            <Text style={[styles.newsCountText, { color: colors.textMuted }]}>
              뉴스 {stock.newsCount}
            </Text>
          </View>
        )}
      </TouchableOpacity>

      {/* 클러스터 목록 또는 빈 상태 */}
      {stock.clusters.length === 0 ? (
        <View style={[styles.emptyCluster, { borderTopColor: colors.divider }]}>
          <Text style={{ color: colors.textMuted, fontSize: 13 }}>최근 주요 뉴스가 없습니다</Text>
        </View>
      ) : (
        <>
          {stock.clusters.slice(0, 3).map((cluster) => (
            <ClusterRow
              key={cluster.clusterId}
              cluster={cluster}
              isDark={isDark}
              colors={colors}
              onPress={() => goToArticles(cluster.clusterId)}
            />
          ))}
          {/* 전체보기 */}
          <TouchableOpacity
            style={[styles.viewAllRow, { borderTopColor: colors.divider }]}
            onPress={goToDetail}
            activeOpacity={0.6}
          >
            <Text style={[styles.viewAllText, { color: colors.textMuted }]}>전체보기</Text>
          </TouchableOpacity>
        </>
      )}
    </View>
  );
}

export default function WatchlistTab() {
  const { colors } = useTheme();

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
      <View style={[styles.header, { borderBottomColor: colors.divider }]}>
        <Text style={[styles.headerTitle, { color: colors.text }]}>관심종목</Text>
        <Text style={[styles.headerSub, { color: colors.textFaint }]}>{WATCHLIST_STOCKS.length}종목</Text>
      </View>
      <FlatList
        data={WATCHLIST_STOCKS}
        renderItem={({ item }) => <StockCard stock={item} />}
        keyExtractor={(item) => item.stockCode}
        contentContainerStyle={{ paddingTop: 8, paddingBottom: 40 }}
      />
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

  // 카드
  card: {
    marginHorizontal: 16,
    marginBottom: 12,
    borderRadius: 14,
    borderWidth: 1,
    overflow: "hidden",
  },

  // 카드 헤더
  cardHeader: {
    flexDirection: "row",
    alignItems: "center",
    padding: 14,
    gap: 8,
  },
  cardHeaderLeft: {
    flex: 1,
  },
  cardHeaderRight: {
    alignItems: "flex-end",
  },
  stockName: {
    fontSize: 16,
    fontWeight: "700",
    letterSpacing: -0.3,
  },
  stockCode: {
    fontSize: 11,
    marginTop: 2,
  },
  price: {
    fontSize: 15,
    fontWeight: "700",
  },
  changeRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4,
    marginTop: 2,
  },
  changeRate: {
    fontSize: 13,
    fontWeight: "600",
  },
  changeAmount: {
    fontSize: 11,
  },
  newsCountBadge: {
    position: "absolute",
    top: 12,
    right: 14,
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
  },
  newsCountText: {
    fontSize: 10,
    fontWeight: "500",
  },

  // 클러스터 행
  clusterRow: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderTopWidth: 1,
    gap: 8,
  },
  clusterLeft: {
    flex: 1,
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
  },
  categoryBadge: {
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
  },
  categoryBadgeText: {
    fontSize: 10,
    fontWeight: "600",
  },
  clusterHeadline: {
    flex: 1,
    fontSize: 13,
    fontWeight: "500",
    letterSpacing: -0.2,
  },
  clusterRight: {
    alignItems: "flex-end",
    gap: 2,
  },
  clusterMeta: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4,
  },
  clusterTime: {
    fontSize: 10,
  },
  clusterArticleCount: {
    fontSize: 10,
  },
  clusterRate: {
    fontSize: 12,
    fontWeight: "700",
  },

  // 빈 상태
  emptyCluster: {
    borderTopWidth: 1,
    paddingVertical: 20,
    alignItems: "center",
  },

  // 전체보기
  viewAllRow: {
    borderTopWidth: 1,
    paddingVertical: 10,
    alignItems: "center",
  },
  viewAllText: {
    fontSize: 12,
    fontWeight: "500",
  },
});
