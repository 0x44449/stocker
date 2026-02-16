package app.sandori.stocker.api.domain.headline;

import app.sandori.stocker.api.entities.NewsRawEntity;
import app.sandori.stocker.api.entities.QNewsExtractionEntity;
import app.sandori.stocker.api.entities.QNewsRawEntity;
import app.sandori.stocker.api.repositories.StockMasterRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 헤드라인 뉴스 조회 서비스.
 * 특정 날짜의 뉴스에서 기업명 언급 횟수를 기반으로 헤드라인 종목을 선정한다.
 */
@Service
public class HeadlineService {

    private final JPAQueryFactory queryFactory;
    private final StockMasterRepository stockMasterRepository;

    public HeadlineService(JPAQueryFactory queryFactory, StockMasterRepository stockMasterRepository) {
        this.queryFactory = queryFactory;
        this.stockMasterRepository = stockMasterRepository;
    }

    // --- DTO ---

    public record ArticleDto(Long newsId, String title, LocalDateTime publishedAt) {}

    public record HeadlineDto(String companyName, int count, List<ArticleDto> articles) {}

    public record HeadlineResponse(LocalDate date, int threshold, long totalNewsCount, List<HeadlineDto> headlines) {}

    /**
     * 헤드라인 뉴스 조회.
     * 해당 날짜의 뉴스에서 기업명 언급 횟수가 threshold 이상인 종목을 선정하고 관련 기사를 반환한다.
     */
    public HeadlineResponse getHeadlines(LocalDate date, int threshold) {
        var n = QNewsRawEntity.newsRawEntity;
        var ext = QNewsExtractionEntity.newsExtractionEntity;

        var startOfDay = date.atStartOfDay();
        var startOfNextDay = date.plusDays(1).atStartOfDay();

        // 1. 해당 날짜의 news_id 목록 조회
        var newsIds = queryFactory.select(n.id)
                .from(n)
                .where(n.publishedAt.goe(startOfDay), n.publishedAt.lt(startOfNextDay))
                .fetch();
        long totalNewsCount = newsIds.size();

        if (newsIds.isEmpty()) {
            return new HeadlineResponse(date, threshold, totalNewsCount, List.of());
        }

        // 2. news_extraction에서 해당 뉴스들의 추출 결과 조회
        var extractions = queryFactory.selectFrom(ext)
                .where(
                        ext.newsId.in(newsIds),
                        ext.llmModel.eq("qwen2.5:7b"),
                        ext.promptVersion.eq("v1")
                )
                .fetch();

        if (extractions.isEmpty()) {
            return new HeadlineResponse(date, threshold, totalNewsCount, List.of());
        }

        // 3. stock_master 종목명 Set 구성
        var stockNames = new HashSet<String>();
        for (var stock : stockMasterRepository.findAll()) {
            stockNames.add(stock.getNameKr());
            stockNames.add(stock.getNameKrShort());
        }

        // 4. keywords를 풀면서 stock_master에 있는 것만 카운팅 (같은 뉴스에서 같은 기업명 중복 제거)
        Map<String, Set<Long>> companyToNewsIds = new HashMap<>();
        for (var extraction : extractions) {
            if (extraction.getKeywords().isEmpty()) {
                continue;
            }
            for (var keyword : extraction.getKeywords()) {
                if (stockNames.contains(keyword)) {
                    companyToNewsIds.computeIfAbsent(keyword, k -> new HashSet<>()).add(extraction.getNewsId());
                }
            }
        }

        // 5. threshold 이상인 종목만 선정, count 내림차순 정렬
        var filteredEntries = companyToNewsIds.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= threshold)
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .toList();

        if (filteredEntries.isEmpty()) {
            return new HeadlineResponse(date, threshold, totalNewsCount, List.of());
        }

        // 헤드라인에 포함된 뉴스만 조회
        var headlineNewsIds = filteredEntries.stream()
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());

        var newsMap = queryFactory.selectFrom(n)
                .where(n.id.in(headlineNewsIds))
                .fetch()
                .stream()
                .collect(Collectors.toMap(NewsRawEntity::getId, e -> e));

        var headlines = filteredEntries.stream()
                .map(entry -> {
                    var articles = entry.getValue().stream()
                            .map(newsId -> {
                                var news = newsMap.get(newsId);
                                return new ArticleDto(news.getId(), news.getTitle(), news.getPublishedAt());
                            })
                            .toList();
                    return new HeadlineDto(entry.getKey(), entry.getValue().size(), articles);
                })
                .toList();

        return new HeadlineResponse(date, threshold, totalNewsCount, headlines);
    }
}
