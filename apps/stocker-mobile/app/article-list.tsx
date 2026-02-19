import { useEffect, useState } from "react";
import { View, Text, FlatList, StyleSheet, TouchableOpacity, Linking, ActivityIndicator } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams, useRouter } from "expo-router";
import { useTheme } from "../src/theme";
import { useAuth } from "../lib/auth";
import { API_BASE_URL } from "../lib/config";

// --- API 응답 타입 ---

interface ArticleDto {
  news_id: number;
  title: string;
  press: string | null;
  url: string | null;
  published_at: string | null;
}

interface StockPriceDto {
  stock_code: string;
  close: number | null;
  diff: number | null;
  diff_rate: number | null;
}

interface RelatedStockDto {
  stock_name: string | null;
  stock_code: string;
  mention_count: number;
  close: number | null;
  diff: number | null;
  diff_rate: number | null;
}

interface TopicDto {
  title: string;
  summary: string | null;
  count: number;
  time: string | null;
  articles: ArticleDto[];
}

interface ClusterDto {
  title: string | null;
  summary: string | null;
  count: number;
  time: string | null;
  articles: ArticleDto[];
}

interface StockTopicsDto {
  stock_code: string;
  stock_name: string;
  total_count: number;
  stock_price: StockPriceDto | null;
  related_stock: RelatedStockDto | null;
  topic: TopicDto | null;
  clusters: ClusterDto[];
}

// --- 화면용 타입 ---

interface ClusterDisplay {
  headline: string;
  summary: string | null;
  time: string | null;
  articleCount: number;
  articles: ArticleDto[];
}

// --- 포맷 헬퍼 ---

function formatChangeRate(rate: number): string {
  const sign = rate > 0 ? "+" : "";
  return `${sign}${rate.toFixed(2)}%`;
}

function formatTime(dateStr: string): string {
  const match = dateStr.match(/T(\d{2}:\d{2})/);
  return match ? match[1] : dateStr;
}

// --- 기사 항목 ---

function ArticleRow({ article, colors }: { article: ArticleDto; colors: any }) {
  const handlePress = () => {
    if (article.url) {
      Linking.openURL(article.url);
    }
  };

  return (
    <TouchableOpacity
      style={styles.articleRow}
      onPress={handlePress}
      activeOpacity={0.6}
      disabled={!article.url}
    >
      <View style={styles.articleTextArea}>
        <Text style={[styles.articleTitle, { color: colors.text }]} numberOfLines={2}>
          {article.title}
        </Text>
        <View style={styles.articleMeta}>
          {article.press && (
            <Text style={[styles.articleSource, { color: colors.textMuted }]}>{article.press}</Text>
          )}
          {article.published_at && (
            <Text style={[styles.articleTime, { color: colors.textFaint }]}>{formatTime(article.published_at)}</Text>
          )}
        </View>
      </View>
    </TouchableOpacity>
  );
}

// --- 클러스터 헤더 ---

function ClusterHeaderView({ cluster, stockName, stockPrice, relatedStock, colors }: {
  cluster: ClusterDisplay;
  stockName: string;
  stockPrice: StockPriceDto | null;
  relatedStock: RelatedStockDto | null;
  colors: any;
}) {
  return (
    <View style={styles.clusterHeader}>
      <View style={styles.clusterTopRow}>
        {cluster.time && (
          <Text style={[styles.clusterTime, { color: colors.textFaint }]}>{cluster.time}</Text>
        )}
        <Text style={[styles.clusterArticleCount, { color: colors.textMuted }]}>
          기사 {cluster.articleCount}건
        </Text>
      </View>

      <Text style={[styles.clusterHeadline, { color: colors.text }]}>{cluster.headline}</Text>
      {cluster.summary && (
        <Text style={[styles.clusterSummary, { color: colors.textSecondary }]}>{cluster.summary}</Text>
      )}

      {/* 관련 종목 */}
      {(stockPrice?.diff_rate != null || relatedStock?.diff_rate != null) && (
        <View style={[styles.relatedStocksArea, { backgroundColor: colors.surface, borderColor: colors.border }]}>
          {stockPrice?.diff_rate != null && (
            <View style={styles.relatedStockRow}>
              <Text style={[styles.relatedStockName, { color: colors.text }]}>{stockName}</Text>
              <Text style={[styles.relatedStockRole, { color: colors.textFaint }]}>주체</Text>
              <Text
                style={[
                  styles.relatedStockRate,
                  { color: stockPrice.diff_rate > 0 ? "#DC2626" : stockPrice.diff_rate < 0 ? "#2563EB" : colors.textMuted },
                ]}
              >
                {formatChangeRate(stockPrice.diff_rate)}
              </Text>
            </View>
          )}
          {relatedStock?.diff_rate != null && relatedStock.stock_name && (
            <View style={styles.relatedStockRow}>
              <Text style={[styles.relatedStockName, { color: colors.text }]}>{relatedStock.stock_name}</Text>
              <Text style={[styles.relatedStockRole, { color: colors.textFaint }]}>관련</Text>
              <Text
                style={[
                  styles.relatedStockRate,
                  { color: relatedStock.diff_rate > 0 ? "#DC2626" : relatedStock.diff_rate < 0 ? "#2563EB" : colors.textMuted },
                ]}
              >
                {formatChangeRate(relatedStock.diff_rate)}
              </Text>
            </View>
          )}
        </View>
      )}
    </View>
  );
}

export default function ArticleListScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const { session } = useAuth();
  const { stockCode, clusterIndex } = useLocalSearchParams<{ stockCode: string; clusterIndex: string }>();
  const [cluster, setCluster] = useState<ClusterDisplay | null>(null);
  const [stockName, setStockName] = useState("");
  const [stockPrice, setStockPrice] = useState<StockPriceDto | null>(null);
  const [relatedStock, setRelatedStock] = useState<RelatedStockDto | null>(null);
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
        if (!res.ok) return;
        const data: StockTopicsDto = await res.json();

        setStockName(data.stock_name ?? stockCode);
        setStockPrice(data.stock_price);
        setRelatedStock(data.related_stock);

        // clusterIndex=0 → topic, 1+ → clusters[index-1]
        const idx = parseInt(clusterIndex ?? "0", 10);
        if (idx === 0 && data.topic) {
          setCluster({
            headline: data.topic.title,
            summary: data.topic.summary,
            time: data.topic.time,
            articleCount: data.topic.count,
            articles: data.topic.articles ?? [],
          });
        } else if (idx > 0 && data.clusters && data.clusters[idx - 1]) {
          const c = data.clusters[idx - 1];
          setCluster({
            headline: c.title ?? `관련 뉴스 ${c.count}건`,
            summary: c.summary,
            time: c.time,
            articleCount: c.count,
            articles: c.articles ?? [],
          });
        }
      } finally {
        setLoading(false);
      }
    }

    fetchData();
  }, [stockCode, clusterIndex]);

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

  if (!cluster) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
        <TouchableOpacity style={styles.backButton} onPress={() => router.back()}>
          <Text style={[styles.backButtonText, { color: colors.text }]}>{"←"}</Text>
        </TouchableOpacity>
        <View style={styles.emptyContainer}>
          <Text style={{ color: colors.textMuted, fontSize: 13 }}>클러스터를 찾을 수 없습니다</Text>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
      <TouchableOpacity style={styles.backButton} onPress={() => router.back()}>
        <Text style={[styles.backButtonText, { color: colors.text }]}>{"←"}</Text>
      </TouchableOpacity>

      <FlatList
        data={cluster.articles}
        renderItem={({ item }) => <ArticleRow article={item} colors={colors} />}
        keyExtractor={(item) => String(item.news_id)}
        ListHeaderComponent={
          <ClusterHeaderView
            cluster={cluster}
            stockName={stockName}
            stockPrice={stockPrice}
            relatedStock={relatedStock}
            colors={colors}
          />
        }
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={{ color: colors.textMuted, fontSize: 13 }}>기사가 없습니다</Text>
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
    paddingVertical: 40,
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

  // 클러스터 헤더
  clusterHeader: {
    paddingHorizontal: 20,
    paddingTop: 4,
    paddingBottom: 14,
  },
  clusterTopRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    marginBottom: 6,
  },
  clusterTime: {
    fontSize: 10,
  },
  clusterArticleCount: {
    fontSize: 10,
  },
  clusterHeadline: {
    fontSize: 16,
    fontWeight: "700",
    lineHeight: 22,
    letterSpacing: -0.3,
    marginBottom: 6,
  },
  clusterSummary: {
    fontSize: 12,
    lineHeight: 18,
    marginBottom: 10,
  },

  // 관련 종목
  relatedStocksArea: {
    borderRadius: 10,
    borderWidth: 1,
    padding: 10,
  },
  relatedStockRow: {
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 3,
    gap: 8,
  },
  relatedStockName: {
    fontSize: 12,
    fontWeight: "600",
    width: 80,
  },
  relatedStockRole: {
    fontSize: 10,
    width: 52,
  },
  relatedStockRate: {
    fontSize: 12,
    fontWeight: "700",
    flex: 1,
    textAlign: "right",
  },

  // 기사 행
  articleRow: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 20,
    paddingVertical: 12,
    gap: 12,
  },
  articleTextArea: {
    flex: 1,
  },
  articleTitle: {
    fontSize: 13,
    fontWeight: "500",
    lineHeight: 18,
    letterSpacing: -0.2,
    marginBottom: 4,
  },
  articleMeta: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
  },
  articleSource: {
    fontSize: 10,
    fontWeight: "500",
  },
  articleTime: {
    fontSize: 10,
  },
});
