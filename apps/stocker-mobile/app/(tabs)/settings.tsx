import { useEffect, useState, useCallback } from "react";
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  ActivityIndicator,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useTheme } from "../../src/theme";
import { useAuth } from "../../lib/auth";
import { API_BASE_URL } from "../../lib/config";

interface StockEntry {
  stockCode: string;
  stockName: string;
}

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

interface StockSearchDto {
  stockCode: string;
  nameKr: string;
  nameKrShort: string;
  market: string;
}

export default function SettingsTab() {
  const { colors, isDark } = useTheme();
  const { session } = useAuth();
  const [searchText, setSearchText] = useState("");
  const [watchlist, setWatchlist] = useState<StockEntry[]>([]);
  const [searchResults, setSearchResults] = useState<StockEntry[]>([]);
  const [loading, setLoading] = useState(true);

  const headers = useCallback((): Record<string, string> => {
    const h: Record<string, string> = { "Content-Type": "application/json" };
    if (session?.access_token) {
      h["Authorization"] = `Bearer ${session.access_token}`;
    }
    return h;
  }, [session]);

  // 관심종목 목록 조회
  const fetchWatchlist = useCallback(async () => {
    try {
      const res = await fetch(`${API_BASE_URL}/api/watchlist`, { headers: headers() });
      if (!res.ok) return;
      const data: WatchlistResponseDto = await res.json();
      setWatchlist(data.stocks.map((s) => ({ stockCode: s.stockCode, stockName: s.stockName })));
    } finally {
      setLoading(false);
    }
  }, [headers]);

  useEffect(() => {
    fetchWatchlist();
  }, [fetchWatchlist]);

  // 종목 검색 (API)
  useEffect(() => {
    const query = searchText.trim();
    if (query.length < 2) {
      setSearchResults([]);
      return;
    }

    const timer = setTimeout(async () => {
      try {
        const res = await fetch(
          `${API_BASE_URL}/api/stocks?query=${encodeURIComponent(query)}`,
          { headers: headers() }
        );
        if (!res.ok) return;
        const data: StockSearchDto[] = await res.json();
        setSearchResults(
          data.map((s) => ({ stockCode: s.stockCode, stockName: s.nameKrShort }))
        );
      } catch {
        // 검색 실패 시 무시
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [searchText, headers]);

  const watchlistCodes = new Set(watchlist.map((s) => s.stockCode));
  const filteredResults = searchResults.filter((s) => !watchlistCodes.has(s.stockCode));

  const handleAdd = async (stock: StockEntry) => {
    try {
      const res = await fetch(`${API_BASE_URL}/api/watchlist`, {
        method: "POST",
        headers: headers(),
        body: JSON.stringify({ stockCode: stock.stockCode }),
      });
      if (res.ok) {
        setWatchlist((prev) => [...prev, stock]);
      }
    } catch {
      // 추가 실패 시 무시
    }
  };

  const handleRemove = async (stockCode: string) => {
    try {
      const res = await fetch(`${API_BASE_URL}/api/watchlist/${stockCode}`, {
        method: "DELETE",
        headers: headers(),
      });
      if (res.ok) {
        setWatchlist((prev) => prev.filter((s) => s.stockCode !== stockCode));
      }
    } catch {
      // 삭제 실패 시 무시
    }
  };

  if (loading) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
        <View style={[styles.header, { borderBottomColor: colors.divider }]}>
          <Text style={[styles.headerTitle, { color: colors.text }]}>관심종목 설정</Text>
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
        <Text style={[styles.headerTitle, { color: colors.text }]}>관심종목 설정</Text>
      </View>

      {/* 검색 입력 */}
      <View style={[styles.searchArea, { borderBottomColor: colors.divider }]}>
        <TextInput
          style={[
            styles.searchInput,
            {
              backgroundColor: colors.surface,
              color: colors.text,
              borderColor: colors.border,
            },
          ]}
          placeholder="종목명 또는 종목코드 검색"
          placeholderTextColor={colors.textFaint}
          value={searchText}
          onChangeText={setSearchText}
          autoCorrect={false}
        />
      </View>

      <FlatList
        data={[]}
        renderItem={null}
        ListHeaderComponent={
          <>
            {/* 검색 결과 */}
            {filteredResults.length > 0 && (
              <View style={styles.section}>
                <Text style={[styles.sectionTitle, { color: colors.textMuted }]}>검색 결과</Text>
                {filteredResults.map((stock) => (
                  <View
                    key={stock.stockCode}
                    style={[styles.stockRow, { borderBottomColor: colors.divider }]}
                  >
                    <View style={styles.stockRowLeft}>
                      <Text style={[styles.stockRowName, { color: colors.text }]}>
                        {stock.stockName}
                      </Text>
                      <Text style={[styles.stockRowCode, { color: colors.textFaint }]}>
                        {stock.stockCode}
                      </Text>
                    </View>
                    <TouchableOpacity
                      style={[styles.addButton, { backgroundColor: isDark ? "#1E3A5C" : "#DBEAFE" }]}
                      onPress={() => handleAdd(stock)}
                      activeOpacity={0.6}
                    >
                      <Text style={[styles.addButtonText, { color: isDark ? "#93C5FD" : "#1E40AF" }]}>
                        추가
                      </Text>
                    </TouchableOpacity>
                  </View>
                ))}
              </View>
            )}

            {/* 내 관심종목 */}
            <View style={styles.section}>
              <Text style={[styles.sectionTitle, { color: colors.textMuted }]}>
                내 관심종목 ({watchlist.length})
              </Text>
              {watchlist.length === 0 ? (
                <View style={styles.emptySection}>
                  <Text style={{ color: colors.textMuted, fontSize: 13 }}>
                    관심종목이 없습니다. 검색하여 추가하세요.
                  </Text>
                </View>
              ) : (
                watchlist.map((stock) => (
                  <View
                    key={stock.stockCode}
                    style={[styles.stockRow, { borderBottomColor: colors.divider }]}
                  >
                    <View style={styles.stockRowLeft}>
                      <Text style={[styles.stockRowName, { color: colors.text }]}>
                        {stock.stockName}
                      </Text>
                      <Text style={[styles.stockRowCode, { color: colors.textFaint }]}>
                        {stock.stockCode}
                      </Text>
                    </View>
                    <TouchableOpacity
                      style={[styles.removeButton, { backgroundColor: isDark ? "#3D2020" : "#FEE2E2" }]}
                      onPress={() => handleRemove(stock.stockCode)}
                      activeOpacity={0.6}
                    >
                      <Text style={[styles.removeButtonText, { color: isDark ? "#FCA5A5" : "#991B1B" }]}>
                        삭제
                      </Text>
                    </TouchableOpacity>
                  </View>
                ))
              )}
            </View>
          </>
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
  header: {
    paddingHorizontal: 20,
    paddingTop: 12,
    paddingBottom: 10,
    borderBottomWidth: 1,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: "800",
    letterSpacing: -0.7,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },

  // 검색
  searchArea: {
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderBottomWidth: 1,
  },
  searchInput: {
    height: 40,
    borderRadius: 10,
    borderWidth: 1,
    paddingHorizontal: 12,
    fontSize: 14,
  },

  // 섹션
  section: {
    paddingTop: 16,
  },
  sectionTitle: {
    fontSize: 12,
    fontWeight: "600",
    paddingHorizontal: 20,
    marginBottom: 8,
    textTransform: "uppercase",
    letterSpacing: 0.5,
  },
  emptySection: {
    paddingVertical: 24,
    alignItems: "center",
  },

  // 종목 행
  stockRow: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderBottomWidth: 1,
  },
  stockRowLeft: {
    flex: 1,
  },
  stockRowName: {
    fontSize: 15,
    fontWeight: "600",
    letterSpacing: -0.2,
  },
  stockRowCode: {
    fontSize: 11,
    marginTop: 2,
  },

  // 버튼
  addButton: {
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: 8,
  },
  addButtonText: {
    fontSize: 13,
    fontWeight: "600",
  },
  removeButton: {
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: 8,
  },
  removeButtonText: {
    fontSize: 13,
    fontWeight: "600",
  },
});
