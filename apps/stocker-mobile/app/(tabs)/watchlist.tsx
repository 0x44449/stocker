import { useState, useCallback } from "react";
import { View, Text, SectionList, StyleSheet, TouchableOpacity, ActivityIndicator, Pressable, Image } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
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

interface ArticleDto {
  news_id: number;
  title: string;
  press: string;
  url: string;
  image_url: string | null;
  published_at: string;
}

interface TopicDto {
  title: string;
  summary: string;
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
  topic: TopicDto | null;
  clusters: ClusterDto[];
}

// --- 화면용 타입 ---

interface ClusterDisplay {
  headline: string;
  summary: string | null;
  time: string | null;
  date: string | null;
  articleCount: number;
  imageUrl: string | null;
}

interface WatchlistStock {
  stockCode: string;
  stockName: string;
  price: number | null;
  changeRate: number | null;
  changeAmount: number | null;
  clusters: ClusterDisplay[];
}

interface StockSection {
  stock: WatchlistStock;
  data: ClusterDisplay[];
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

// "HH:mm" → "오전/오후 h:mm"
function formatTime(time: string): string {
  const [hStr, mStr] = time.split(":");
  const h = parseInt(hStr, 10);
  const period = h < 12 ? "오전" : "오후";
  const displayH = h === 0 ? 12 : h > 12 ? h - 12 : h;
  return `${period} ${displayH}:${mStr}`;
}

// articles에서 가장 빠른 published_at의 날짜를 "MM-DD" 형식으로 추출. 오늘이면 null 반환
function extractDate(articles: ArticleDto[]): string | null {
  const earliest = articles
    ?.filter(a => a.published_at)
    .map(a => a.published_at)
    .sort()[0];
  if (!earliest) return null;
  const date = earliest.substring(0, 10); // "yyyy-MM-dd"
  const today = new Date().toISOString().substring(0, 10);
  if (date === today) return null;
  const yesterday = new Date(Date.now() - 86400000).toISOString().substring(0, 10);
  if (date === yesterday) return "어제";
  // "MM/DD"
  const [m, d] = date.substring(5).split("-");
  return `${m}/${d}`;
}

// --- 섹션 헤더 (종목 정보) ---

function SectionHeader({ stock }: { stock: WatchlistStock }) {
  const { colors } = useTheme();
  const router = useRouter();
  const isUp = (stock.changeRate ?? 0) > 0;

  const goToDetail = () => {
    router.push({ pathname: "/stock-detail", params: { stockCode: stock.stockCode } });
  };

  return (
    <TouchableOpacity style={[styles.sectionHeader, { backgroundColor: colors.bg }]} onPress={goToDetail} activeOpacity={0.6}>
      <View style={styles.sectionHeaderLeft}>
        <Text style={[styles.stockName, { color: colors.text }]}>{stock.stockName}</Text>
        <Text style={[styles.stockCode, { color: colors.textTertiary }]}>{stock.stockCode}</Text>
      </View>
      <View style={styles.sectionHeaderRight}>
        {stock.price != null ? (
          <>
            <Text style={[styles.price, { color: colors.text }]}>{formatPrice(stock.price)}</Text>
            <View style={styles.changeRow}>
              {stock.changeRate != null && (
                <Text style={[styles.changeRate, { color: isUp ? "#E53935" : "#1E88E5" }]}>
                  {formatChangeRate(stock.changeRate)}
                </Text>
              )}
              {stock.changeAmount != null && (
                <Text style={[styles.changeAmount, { color: isUp ? "#E53935" : "#1E88E5" }]}>
                  {formatChangeAmount(stock.changeAmount)}
                </Text>
              )}
            </View>
          </>
        ) : (
          <Text style={{ color: colors.textMuted, fontSize: 16 }}>-</Text>
        )}
      </View>
    </TouchableOpacity>
  );
}

// --- 클러스터 행 ---

function ClusterRow({ cluster, stockCode, clusterIndex, colors }: {
  cluster: ClusterDisplay;
  stockCode: string;
  clusterIndex: number;
  colors: any;
}) {
  const router = useRouter();

  const goToArticleList = () => {
    router.push({ pathname: "/article-list", params: { stockCode, clusterIndex: String(clusterIndex) } });
  };

  const imageUri = cluster.imageUrl ? `${API_BASE_URL}${cluster.imageUrl}` : null;

  // 메타 텍스트 조합: "어제 오전 10:30 3건"
  const metaParts: string[] = [];
  if (cluster.date) metaParts.push(cluster.date);
  if (cluster.time) metaParts.push(formatTime(cluster.time));
  metaParts.push(`${cluster.articleCount}건`);
  const metaText = metaParts.join(" ");

  return (
    <Pressable style={styles.clusterRow} onPress={goToArticleList}>
      <View style={styles.clusterBody}>
        {imageUri && (
          <Image source={{ uri: imageUri }} style={styles.clusterImage} />
        )}
        <View style={styles.clusterContent}>
          <Text style={[styles.clusterHeadline, { color: colors.text }]} numberOfLines={2}>
            {cluster.headline}
          </Text>
          <Text style={[styles.clusterMeta, { color: colors.textTertiary }]}>{metaText}</Text>
          {cluster.summary && (
            <Text style={[styles.clusterSummary, { color: colors.textTertiary }]} numberOfLines={3}>
              {cluster.summary}
            </Text>
          )}
        </View>
      </View>
    </Pressable>
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
        const firstImage = topics.topic.articles?.find(a => a.image_url)?.image_url ?? null;
        clusters.push({
          headline: topics.topic.title,
          summary: topics.topic.summary ?? null,
          time: topics.topic.time,
          date: extractDate(topics.topic.articles ?? []),
          articleCount: topics.topic.count,
          imageUrl: firstImage,
        });
      }
      if (topics?.clusters) {
        for (const c of topics.clusters) {
          const firstImage = c.articles?.find(a => a.image_url)?.image_url ?? null;
          clusters.push({
            headline: c.title ?? `관련 뉴스 ${c.count}건`,
            summary: c.summary ?? null,
            time: c.time,
            date: extractDate(c.articles ?? []),
            articleCount: c.count,
            imageUrl: firstImage,
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

  const sections: StockSection[] = data.map((stock) => ({
    stock,
    data: stock.clusters.length > 0 ? stock.clusters.slice(0, 10) : [null as any],
  }));

  if (loading) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]} edges={["top", "left", "right"]}>
        <View style={styles.header}>
          <Text style={[styles.headerTitle, { color: colors.text }]}>관심종목</Text>
        </View>
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color={colors.textMuted} />
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]} edges={["top", "left", "right"]}>
      <View style={styles.header}>
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
        <SectionList
          sections={sections}
          keyExtractor={(item, index) => String(index)}
          renderSectionHeader={({ section }) => (
            <SectionHeader stock={section.stock} />
          )}
          renderItem={({ item, index, section }) => {
            if (!item) {
              return (
                <View style={styles.emptyCluster}>
                  <Text style={{ color: colors.textMuted, fontSize: 14 }}>최근 주요 뉴스가 없습니다</Text>
                </View>
              );
            }
            return (
              <ClusterRow
                cluster={item}
                stockCode={section.stock.stockCode}
                clusterIndex={index}
                colors={colors}
              />
            );
          }}
          contentContainerStyle={{ paddingBottom: 0 }}
          stickySectionHeadersEnabled={true}
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
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 10,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  headerTitle: {
    fontSize: 28,
    fontWeight: "800",
    lineHeight: 36,
    letterSpacing: -0.7,
  },
  headerSub: {
    fontSize: 14,
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

  // 섹션 헤더 (종목 정보)
  sectionHeader: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 16,
    paddingVertical: 14,
  },
  sectionHeaderLeft: {
    flex: 1,
  },
  sectionHeaderRight: {
    alignItems: "flex-end",
  },
  stockName: {
    fontSize: 20,
    fontWeight: "700",
    letterSpacing: -0.3,
  },
  stockCode: {
    fontSize: 14,
    marginTop: 2,
  },
  price: {
    fontSize: 18,
    fontWeight: "700",
  },
  changeRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4,
    marginTop: 2,
  },
  changeRate: {
    fontSize: 16,
    fontWeight: "600",
  },
  changeAmount: {
    fontSize: 14,
  },

  // 클러스터 행
  clusterRow: {
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  clusterBody: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
  },
  clusterImage: {
    width: 90,
    height: 65,
    borderRadius: 6,
    backgroundColor: "#E0E0E0",
  },
  clusterContent: {
    flex: 1,
    gap: 4,
  },
  clusterHeadline: {
    fontSize: 14,
    fontWeight: "500",
    letterSpacing: -0.2,
  },
  clusterSummary: {
    fontSize: 12,
  },
  clusterMeta: {
    fontSize: 12,
  },

  // 빈 상태
  emptyCluster: {
    paddingVertical: 20,
    alignItems: "center",
  },
});
