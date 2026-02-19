package app.sandori.stocker.api.domain.feed;

import app.sandori.stocker.api.entities.QNewsExtractionEntity;
import app.sandori.stocker.api.repositories.StockMasterRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 오늘의 핫 종목 서비스.
 * 오늘 뉴스에서 가장 많이 언급된 종목 상위 3개를 반환한다.
 */
@Service
public class HotStockService {

    private static final int TOP_COUNT = 3;

    private final JPAQueryFactory queryFactory;
    private final StockMasterRepository stockMasterRepository;

    public HotStockService(JPAQueryFactory queryFactory, StockMasterRepository stockMasterRepository) {
        this.queryFactory = queryFactory;
        this.stockMasterRepository = stockMasterRepository;
    }

    // --- DTO ---

    public record HotStock(int rank, String stockCode, String stockName, int count) {}

    public record HotStockResponse(List<HotStock> stocks) {}

    /**
     * 오늘의 핫 종목 조회.
     */
    public HotStockResponse getHotStocks() {
        var ext = QNewsExtractionEntity.newsExtractionEntity;

        var today = LocalDate.now();
        var startOfDay = today.minusDays(1).atStartOfDay();
        var startOfNextDay = today.plusDays(1).atStartOfDay();

        // 1. news_extraction에서 published_at이 오늘인 것 조회
        var extractions = queryFactory.selectFrom(ext)
                .where(
                        ext.publishedAt.goe(startOfDay),
                        ext.publishedAt.lt(startOfNextDay),
                        ext.llmModel.eq("exaone3.5:7.8b"),
                        ext.promptVersion.eq("v1")
                )
                .fetch();

        if (extractions.isEmpty()) {
            return new HotStockResponse(List.of());
        }

        // 2. stock_master에서 종목명 → 종목코드 매핑 구성
        Map<String, String> nameToCode = new HashMap<>();
        for (var stock : stockMasterRepository.findAll()) {
            nameToCode.put(stock.getNameKr(), stock.getStockCode());
            nameToCode.put(stock.getNameKrShort(), stock.getStockCode());
        }

        // 3. keywords 풀어서 stock_master에 있는 것만 종목코드 기준으로 카운팅
        Map<String, Set<Long>> codeToNewsIds = new HashMap<>();
        for (var extraction : extractions) {
            if (extraction.getKeywords().isEmpty()) {
                continue;
            }
            for (var keyword : extraction.getKeywords()) {
                var code = nameToCode.get(keyword);
                if (code != null) {
                    codeToNewsIds.computeIfAbsent(code, k -> new HashSet<>()).add(extraction.getNewsId());
                }
            }
        }

        // 4. 종목코드 → 종목명(short) 매핑
        Map<String, String> codeToName = new HashMap<>();
        for (var stock : stockMasterRepository.findAll()) {
            codeToName.putIfAbsent(stock.getStockCode(), stock.getNameKrShort());
        }

        // 5. 상위 3개만 반환
        var stocks = codeToNewsIds.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .limit(TOP_COUNT)
                .toList();

        var result = new java.util.ArrayList<HotStock>();
        for (int i = 0; i < stocks.size(); i++) {
            var entry = stocks.get(i);
            result.add(new HotStock(i + 1, entry.getKey(), codeToName.getOrDefault(entry.getKey(), entry.getKey()), entry.getValue().size()));
        }

        return new HotStockResponse(result);
    }
}
