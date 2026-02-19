import { View, Text, FlatList, StyleSheet, TouchableOpacity } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { LinearGradient } from "expo-linear-gradient";
import { useRouter } from "expo-router";
import { useTheme } from "../../src/theme";
import { WATCHLIST_STOCKS, WatchlistStock, ClusterItem } from "../../src/mock/watchlistMock";

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
function ClusterRow({ cluster, colors, onPress }: {
  cluster: ClusterItem;
  colors: any;
  onPress: () => void;
}) {

  return (
    <TouchableOpacity
      style={styles.clusterRow}
      onPress={onPress}
      activeOpacity={0.6}
    >
      <Text style={[styles.clusterHeadline, { color: colors.text }]} numberOfLines={2}>
        {cluster.headline}
      </Text>
      <View style={styles.clusterMeta}>
        <Text style={[styles.clusterTime, { color: colors.textFaint }]}>{cluster.time}</Text>
        <Text style={[styles.clusterArticleCount, { color: colors.textMuted }]}>
          {cluster.articleCount}건
        </Text>
      </View>
    </TouchableOpacity>
  );
}

// 종목 카드
function StockCard({ stock }: { stock: WatchlistStock }) {
  const { colors } = useTheme();
  const router = useRouter();
  const isUp = stock.changeRate > 0;

  const goToDetail = () => {
    router.push({ pathname: "/stock-detail", params: { stockCode: stock.stockCode } });
  };

  const goToArticles = (clusterId: string) => {
    router.push({ pathname: "/article-list", params: { clusterId } });
  };

  return (
    <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
      {/* 카드 헤더 - 이미지 영역 */}
      <TouchableOpacity style={styles.cardHeader} onPress={goToDetail} activeOpacity={0.6}>
        <LinearGradient
          colors={["#1a1a2e", "#16213e", "#0f3460", "#533483"]}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 1 }}
          style={StyleSheet.absoluteFill}
        >
          {/* TODO: 종목별 대표 이미지 영역 */}
          <View style={[styles.decoCircle, { width: 140, height: 140, borderRadius: 70, right: -20, top: -40, opacity: 0.08 }]} />
          <View style={[styles.decoCircle, { width: 80, height: 80, borderRadius: 40, right: 10, bottom: -10, opacity: 0.06 }]} />
        </LinearGradient>
        <View style={styles.cardHeaderContent}>
          <View style={styles.cardHeaderTop}>
            <View style={styles.cardHeaderLeft}>
              <Text style={[styles.stockName, { color: "#FFFFFF" }]}>{stock.stockName}</Text>
              <Text style={[styles.stockCode, { color: "rgba(255,255,255,0.6)" }]}>{stock.stockCode}</Text>
            </View>
            <View style={styles.cardHeaderRight}>
              <Text style={[styles.price, { color: "#FFFFFF" }]}>{formatPrice(stock.price)}</Text>
              <View style={styles.changeRow}>
                <Text style={[styles.changeRate, { color: isUp ? "#FCA5A5" : "#93C5FD" }]}>
                  {formatChangeRate(stock.changeRate)}
                </Text>
                <Text style={[styles.changeAmount, { color: isUp ? "#FCA5A5" : "#93C5FD" }]}>
                  {formatChangeAmount(stock.changeAmount)}
                </Text>
              </View>
            </View>
          </View>
        </View>
      </TouchableOpacity>

      {/* 클러스터 목록 또는 빈 상태 */}
      {stock.clusters.length === 0 ? (
        <View style={styles.emptyCluster}>
          <Text style={{ color: colors.textMuted, fontSize: 13 }}>최근 주요 뉴스가 없습니다</Text>
        </View>
      ) : (
        stock.clusters.slice(0, 3).map((cluster) => (
          <ClusterRow
            key={cluster.clusterId}
            cluster={cluster}
            colors={colors}
            onPress={() => goToArticles(cluster.clusterId)}
          />
        ))
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
    minHeight: 90,
    overflow: "hidden",
  },
  decoCircle: {
    position: "absolute",
    backgroundColor: "white",
  },
  cardHeaderContent: {
    flex: 1,
    justifyContent: "flex-end",
    padding: 14,
  },
  cardHeaderTop: {
    flexDirection: "row",
    alignItems: "flex-end",
    justifyContent: "space-between",
  },
  highlightArea: {
    marginLeft: 14,
    marginRight: 14,
    marginTop: 10,
    paddingLeft: 10,
    paddingVertical: 4,
    borderLeftWidth: 3,
  },
  highlightMain: {
    fontSize: 14,
    fontWeight: "700",
    letterSpacing: -0.3,
  },
  highlightSub: {
    fontSize: 12,
    marginTop: 2,
  },
  cardHeaderLeft: {
    flex: 1,
  },
  cardHeaderRight: {
    alignItems: "flex-end",
  },
  stockName: {
    fontSize: 18,
    fontWeight: "800",
    letterSpacing: -0.3,
  },
  stockCode: {
    fontSize: 11,
    marginTop: 2,
  },
  price: {
    fontSize: 16,
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

  // 클러스터 행
  clusterRow: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 14,
    paddingVertical: 14,
    gap: 8,
  },
  clusterHeadline: {
    flex: 1,
    fontSize: 13,
    fontWeight: "500",
    letterSpacing: -0.2,
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

  // 빈 상태
  emptyCluster: {
    paddingVertical: 20,
    alignItems: "center",
  },

});
