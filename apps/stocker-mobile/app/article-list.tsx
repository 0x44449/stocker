import { View, Text, FlatList, StyleSheet, TouchableOpacity, Linking } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams } from "expo-router";
import { useTheme } from "../src/theme";
import { findClusterById, ArticleItem } from "../src/mock/watchlistMock";

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
      style={[styles.articleRow, { borderBottomColor: colors.divider }]}
      onPress={handlePress}
      activeOpacity={0.6}
    >
      <Text style={[styles.articleTitle, { color: colors.text }]} numberOfLines={2}>
        {article.title}
      </Text>
      <View style={styles.articleMeta}>
        <Text style={[styles.articleSource, { color: colors.textMuted }]}>{article.source}</Text>
        <Text style={[styles.articleTime, { color: colors.textFaint }]}>{article.time}</Text>
      </View>
    </TouchableOpacity>
  );
}

export default function ArticleListScreen() {
  const { colors, isDark } = useTheme();
  const { clusterId } = useLocalSearchParams<{ clusterId: string }>();

  const result = findClusterById(clusterId ?? "");

  if (!result) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
        <View style={styles.emptyContainer}>
          <Text style={{ color: colors.textMuted, fontSize: 14 }}>클러스터를 찾을 수 없습니다</Text>
        </View>
      </SafeAreaView>
    );
  }

  const { stock, cluster } = result;
  const catStyle = getCategoryStyle(cluster.category, isDark);

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
      {/* 클러스터 정보 헤더 */}
      <View style={[styles.clusterHeader, { borderBottomColor: colors.divider }]}>
        <View style={styles.clusterTopRow}>
          <View style={[styles.categoryBadge, { backgroundColor: catStyle.bg }]}>
            <Text style={[styles.categoryBadgeText, { color: catStyle.color }]}>{cluster.category}</Text>
          </View>
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

      {/* 기사 목록 */}
      <FlatList
        data={cluster.articles}
        renderItem={({ item }) => <ArticleRow article={item} colors={colors} />}
        keyExtractor={(item) => item.id}
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

  // 클러스터 헤더
  clusterHeader: {
    paddingHorizontal: 20,
    paddingTop: 12,
    paddingBottom: 14,
    borderBottomWidth: 1,
  },
  clusterTopRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    marginBottom: 8,
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
  clusterTime: {
    fontSize: 11,
  },
  clusterArticleCount: {
    fontSize: 11,
  },
  clusterHeadline: {
    fontSize: 18,
    fontWeight: "800",
    lineHeight: 26,
    letterSpacing: -0.5,
    marginBottom: 8,
  },
  clusterSummary: {
    fontSize: 13,
    lineHeight: 20,
    marginBottom: 12,
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
    paddingVertical: 4,
    gap: 8,
  },
  relatedStockName: {
    fontSize: 13,
    fontWeight: "600",
    width: 80,
  },
  relatedStockRole: {
    fontSize: 10,
    width: 52,
  },
  relatedStockRate: {
    fontSize: 13,
    fontWeight: "700",
    flex: 1,
    textAlign: "right",
  },

  // 기사 행
  articleRow: {
    paddingHorizontal: 20,
    paddingVertical: 14,
    borderBottomWidth: 1,
  },
  articleTitle: {
    fontSize: 14,
    fontWeight: "500",
    lineHeight: 20,
    letterSpacing: -0.2,
    marginBottom: 4,
  },
  articleMeta: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  articleSource: {
    fontSize: 11,
    fontWeight: "500",
  },
  articleTime: {
    fontSize: 11,
  },
});
