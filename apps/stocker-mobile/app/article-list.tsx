import { View, Text, FlatList, StyleSheet, TouchableOpacity, Linking, Image } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams, useRouter } from "expo-router";
import { useTheme } from "../src/theme";
import { findClusterById, ArticleItem, WatchlistStock, ClusterItem } from "../src/mock/watchlistMock";

function formatChangeRate(rate: number): string {
  const sign = rate > 0 ? "+" : "";
  return `${sign}${rate.toFixed(2)}%`;
}

// 기사 항목
function ArticleRow({ article, colors }: { article: ArticleItem; colors: any }) {
  const handlePress = () => {
    Linking.openURL(article.url);
  };

  return (
    <TouchableOpacity
      style={styles.articleRow}
      onPress={handlePress}
      activeOpacity={0.6}
    >
      {/* 썸네일 */}
      {article.thumbnailUrl ? (
        <Image source={{ uri: article.thumbnailUrl }} style={[styles.thumbnail, { backgroundColor: colors.surface }]} />
      ) : (
        <View style={[styles.thumbnail, { backgroundColor: colors.surface }]} />
      )}

      {/* 기사 텍스트 */}
      <View style={styles.articleTextArea}>
        <Text style={[styles.articleTitle, { color: colors.text }]} numberOfLines={2}>
          {article.title}
        </Text>
        <View style={styles.articleMeta}>
          <Text style={[styles.articleSource, { color: colors.textMuted }]}>{article.source}</Text>
          <Text style={[styles.articleTime, { color: colors.textFaint }]}>{article.time}</Text>
        </View>
      </View>
    </TouchableOpacity>
  );
}

// 클러스터 헤더 (FlatList ListHeaderComponent)
function ClusterHeader({ stock, cluster, colors }: {
  stock: WatchlistStock;
  cluster: ClusterItem;
  colors: any;
}) {
  return (
    <View style={styles.clusterHeader}>
        <View style={styles.clusterTopRow}>
          <Text style={[styles.clusterTime, { color: colors.textFaint }]}>{cluster.time}</Text>
          <Text style={[styles.clusterArticleCount, { color: colors.textMuted }]}>
            기사 {cluster.articleCount}건
          </Text>
        </View>

        <Text style={[styles.clusterHeadline, { color: colors.text }]}>{cluster.headline}</Text>
        <Text style={[styles.clusterSummary, { color: colors.textSecondary }]}>{cluster.summary}</Text>

        {/* 관련 종목 */}
        {cluster.relatedStocks.length > 0 && (
          <View style={[styles.relatedStocksArea, { backgroundColor: colors.surface, borderColor: colors.border }]}>
            <View style={styles.relatedStockRow}>
              <Text style={[styles.relatedStockName, { color: colors.text }]}>{stock.stockName}</Text>
              <Text style={[styles.relatedStockRole, { color: colors.textFaint }]}>주체</Text>
              <Text
                style={[
                  styles.relatedStockRate,
                  { color: stock.changeRate > 0 ? "#DC2626" : stock.changeRate < 0 ? "#2563EB" : colors.textMuted },
                ]}
              >
                {formatChangeRate(stock.changeRate)}
              </Text>
            </View>
            {cluster.relatedStocks.map((rs) => {
              const rsRateColor = rs.changeRate > 0 ? "#DC2626" : rs.changeRate < 0 ? "#2563EB" : colors.textMuted;
              return (
                <View key={rs.name} style={styles.relatedStockRow}>
                  <Text style={[styles.relatedStockName, { color: colors.text }]}>{rs.name}</Text>
                  <Text style={[styles.relatedStockRole, { color: colors.textFaint }]}>{rs.role}</Text>
                  <Text style={[styles.relatedStockRate, { color: rsRateColor }]}>
                    {formatChangeRate(rs.changeRate)}
                  </Text>
                </View>
              );
            })}
          </View>
        )}
    </View>
  );
}

export default function ArticleListScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const { clusterId } = useLocalSearchParams<{ clusterId: string }>();

  const result = findClusterById(clusterId ?? "");

  if (!result) {
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

  const { stock, cluster } = result;

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
      {/* 고정 헤더: 뒤로가기 */}
      <TouchableOpacity style={styles.backButton} onPress={() => router.back()}>
        <Text style={[styles.backButtonText, { color: colors.text }]}>{"←"}</Text>
      </TouchableOpacity>

      <FlatList
        data={cluster.articles}
        renderItem={({ item }) => <ArticleRow article={item} colors={colors} />}
        keyExtractor={(item) => item.id}
        ListHeaderComponent={
          <ClusterHeader stock={stock} cluster={cluster} colors={colors} />
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
  thumbnail: {
    width: 58,
    height: 58,
    borderRadius: 8,
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
