import { useState, useCallback } from "react";
import { View, Text, FlatList, StyleSheet, TouchableOpacity, ActivityIndicator } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { LinearGradient } from "expo-linear-gradient";
import { useRouter } from "expo-router";
import { useFocusEffect } from "@react-navigation/native";
import { useTheme } from "../../src/theme";
import { useAuth } from "../../lib/auth";
import { API_BASE_URL } from "../../lib/config";

// --- API 응답 타입 ---

interface WatchlistItemDto {
  stockCode: string;
  stockName: string;
  sortOrder: number;
  addedAt: string;
}

interface WatchlistResponseDto {
  stocks: WatchlistItemDto[];
}

interface StockPriceDto {
  stock_code: string;
  date: string;
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

// --- 화면용 타입 ---

interface ClusterDisplay {
  headline: string;
  time: string | null;
  articleCount: number;
}

interface WatchlistStock {
  stockCode: string;
  stockName: string;
  price: number | null;
  changeRate: number | null;
  changeAmount: number | null;
  clusters: ClusterDisplay[];
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

// --- 클러스터 행 ---

function ClusterRow({ cluster, colors }: {
  cluster: ClusterDisplay;
  colors: any;
}) {
  return (
    <View style={styles.clusterRow}>
      <Text style={[styles.clusterHeadline, { color: colors.text }]} numberOfLines={2}>
        {cluster.headline}
      </Text>
      <View style={styles.clusterMeta}>
        {cluster.time && (
          <Text style={[styles.clusterTime, { color: colors.textFaint }]}>{cluster.time}</Text>
        )}
        <Text style={[styles.clusterArticleCount, { color: colors.textMuted }]}>
          {cluster.articleCount}건
        </Text>
      </View>
    </View>
  );
}

// --- 종목 카드 ---

function StockCard({ stock }: { stock: WatchlistStock }) {
  const { colors } = useTheme();
  const router = useRouter();
  const isUp = (stock.changeRate ?? 0) > 0;

  const goToDetail = () => {
    router.push({ pathname: "/stock-detail", params: { stockCode: stock.stockCode } });
  };

  return (
    <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
      {/* 카드 헤더 */}
      <TouchableOpacity style={styles.cardHeader} onPress={goToDetail} activeOpacity={0.6}>
        <LinearGradient
          colors={["#1a1a2e", "#16213e", "#0f3460", "#533483"]}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 1 }}
          style={StyleSheet.absoluteFill}
        >
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
              {stock.price != null ? (
                <>
                  <Text style={[styles.price, { color: "#FFFFFF" }]}>{formatPrice(stock.price)}</Text>
                  <View style={styles.changeRow}>
                    {stock.changeRate != null && (
                      <Text style={[styles.changeRate, { color: isUp ? "#FCA5A5" : "#93C5FD" }]}>
                        {formatChangeRate(stock.changeRate)}
                      </Text>
                    )}
                    {stock.changeAmount != null && (
                      <Text style={[styles.changeAmount, { color: isUp ? "#FCA5A5" : "#93C5FD" }]}>
                        {formatChangeAmount(stock.changeAmount)}
                      </Text>
                    )}
                  </View>
                </>
              ) : (
                <Text style={{ color: "rgba(255,255,255,0.5)", fontSize: 13 }}>-</Text>
              )}
            </View>
          </View>
        </View>
      </TouchableOpacity>

      {/* 클러스터 목록 */}
      {stock.clusters.length === 0 ? (
        <View style={styles.emptyCluster}>
          <Text style={{ color: colors.textMuted, fontSize: 13 }}>최근 주요 뉴스가 없습니다</Text>
        </View>
      ) : (
        stock.clusters.slice(0, 3).map((cluster, i) => (
          <ClusterRow key={i} cluster={cluster} colors={colors} />
        ))
      )}
    </View>
  );
}

// --- 메인 화면 ---

export default function WatchlistTab() {
  const { colors } = useTheme();
  const { session } = useAuth();
  const [data, setData] = useState<WatchlistStock[]>([]);
  const [loading, setLoading] = useState(true);

  const authHeaders = useCallback((): Record<string, string> => {
    const h: Record<string, string> = {};
    if (session?.access_token) {
      h["Authorization"] = `Bearer ${session.access_token}`;
    }
    return h;
  }, [session]);

  useFocusEffect(
    useCallback(() => {
      fetchData();
    }, [])
  );

  async function fetchData() {
    setLoading(true);
    const h = authHeaders();
    console.log("[watchlist] access_token:", session?.access_token);

    // 1. 관심종목 목록 조회
    let watchlistItems: WatchlistItemDto[];
    try {
      const res = await fetch(`${API_BASE_URL}/api/watchlist`, { headers: h });
      if (!res.ok) {
        setLoading(false);
        return;
      }
      const body: WatchlistResponseDto = await res.json();
      watchlistItems = body.stocks;
    } catch {
      setLoading(false);
      return;
    }

    if (watchlistItems.length === 0) {
      setData([]);
      setLoading(false);
      return;
    }

    // 2. 각 종목별 stock-topics 조회
    const results = await Promise.allSettled(
      watchlistItems.map((item) =>
        fetch(`${API_BASE_URL}/api/feed/stock-topics?stockCode=${item.stockCode}`, { headers: h })
          .then((res) => res.json() as Promise<StockTopicsDto>)
      )
    );

    // 3. 데이터 조합
    const stocks: WatchlistStock[] = watchlistItems.map((item, i) => {
      const result = results[i];
      const topics = result.status === "fulfilled" ? result.value : null;

      const clusters: ClusterDisplay[] = [];
      if (topics?.topic) {
        clusters.push({
          headline: topics.topic.title,
          time: topics.topic.time,
          articleCount: topics.topic.count,
        });
      }
      if (topics?.clusters) {
        for (const c of topics.clusters) {
          clusters.push({
            headline: c.title ?? `관련 뉴스 ${c.count}건`,
            time: c.time,
            articleCount: c.count,
          });
        }
      }

      return {
        stockCode: item.stockCode,
        stockName: item.stockName,
        price: topics?.stock_price?.close ?? null,
        changeRate: topics?.stock_price?.diff_rate ?? null,
        changeAmount: topics?.stock_price?.diff ?? null,
        clusters,
      };
    });

    setData(stocks);
    setLoading(false);
  }

  if (loading) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
        <View style={[styles.header, { borderBottomColor: colors.divider }]}>
          <Text style={[styles.headerTitle, { color: colors.text }]}>관심종목</Text>
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
        <Text style={[styles.headerTitle, { color: colors.text }]}>관심종목</Text>
        <Text style={[styles.headerSub, { color: colors.textFaint }]}>{data.length}종목</Text>
      </View>
      {data.length === 0 ? (
        <View style={styles.emptyContainer}>
          <Text style={{ color: colors.textMuted, fontSize: 14 }}>
            관심종목을 설정 탭에서 추가하세요
          </Text>
        </View>
      ) : (
        <FlatList
          data={data}
          renderItem={({ item }) => <StockCard stock={item} />}
          keyExtractor={(item) => item.stockCode}
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
