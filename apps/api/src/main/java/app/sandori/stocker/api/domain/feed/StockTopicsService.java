package app.sandori.stocker.api.domain.feed;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.sandori.stocker.api.entities.NewsRawEntity;
import app.sandori.stocker.api.entities.StockClusterResultEntity;
import app.sandori.stocker.api.entities.StockPriceDailyRawEntity;
import app.sandori.stocker.api.repositories.NewsRawRepository;
import app.sandori.stocker.api.repositories.StockClusterResultRepository;
import app.sandori.stocker.api.repositories.StockPriceDailyRawRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 종목별 뉴스 클러스터링 서비스.
 * DB에 저장된 클러스터링 결과를 조회하고, news_raw에서 기사 상세 정보를 보강하여 반환한다.
 */
@Service
public class StockTopicsService {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final StockClusterResultRepository repository;
    private final NewsRawRepository newsRawRepository;
    private final StockPriceDailyRawRepository priceRepository;

    public StockTopicsService(StockClusterResultRepository repository, NewsRawRepository newsRawRepository,
                              StockPriceDailyRawRepository priceRepository) {
        this.repository = repository;
        this.newsRawRepository = newsRawRepository;
        this.priceRepository = priceRepository;
    }

    // --- JSONB 역직렬화용 private records ---

    private record RawArticleDto(@JsonProperty("news_id") long newsId, String title) {}
    private record RawTopicDto(String title, String summary, int count, List<RawArticleDto> articles) {}
    private record RawClusterDto(String title, int count, List<RawArticleDto> articles) {}
    private record RawRelatedStockDto(
            @JsonProperty("stock_name") String stockName,
            @JsonProperty("stock_code") String stockCode,
            @JsonProperty("mention_count") int mentionCount
    ) {}
    private record RawStockTopicsDto(
            String keyword,
            @JsonProperty("total_count") int totalCount,
            @JsonProperty("related_stock") RawRelatedStockDto relatedStock,
            RawTopicDto topic,
            List<RawClusterDto> clusters,
            List<RawArticleDto> noise
    ) {}

    // --- 응답용 public records ---

    public record ArticleDto(
            @JsonProperty("news_id") long newsId,
            String title,
            String press,
            String url,
            @JsonProperty("published_at") LocalDateTime publishedAt
    ) {}

    public record StockPriceDto(
            @JsonProperty("stock_code") String stockCode,
            String date,
            Long close,
            Long diff,
            @JsonProperty("diff_rate") Double diffRate
    ) {}

    public record RelatedStockDto(
            @JsonProperty("stock_name") String stockName,
            @JsonProperty("stock_code") String stockCode,
            @JsonProperty("mention_count") int mentionCount,
            Long close,
            Long diff,
            @JsonProperty("diff_rate") Double diffRate
    ) {}

    public record TopicDto(String title, String summary, int count, String time, List<ArticleDto> articles) {}

    public record ClusterDto(String title, int count, String time, List<ArticleDto> articles) {}

    public record StockTopicsDto(
            @JsonProperty("stock_code") String stockCode,
            @JsonProperty("stock_name") String stockName,
            @JsonProperty("total_count") int totalCount,
            @JsonProperty("stock_price") StockPriceDto stockPrice,
            @JsonProperty("related_stock") RelatedStockDto relatedStock,
            TopicDto topic,
            List<ClusterDto> clusters,
            List<ArticleDto> noise
    ) {}

    public StockTopicsDto getStockTopics(String stockCode) {
        StockClusterResultEntity entity = repository.findFirstByStockCodeOrderByClusteredAtDesc(stockCode);
        if (entity == null) {
            return new StockTopicsDto(stockCode, null, 0, null, null, null, List.of(), List.of());
        }

        RawStockTopicsDto raw;
        try {
            raw = objectMapper.readValue(entity.getResult(), RawStockTopicsDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("클러스터링 결과 역직렬화 실패", e);
        }

        // 모든 article에서 newsId 수집
        List<Long> newsIds = Stream.of(
                raw.topic() != null && raw.topic().articles() != null
                        ? raw.topic().articles().stream().map(RawArticleDto::newsId)
                        : Stream.<Long>empty(),
                raw.clusters() != null
                        ? raw.clusters().stream()
                            .flatMap(c -> c.articles() != null ? c.articles().stream() : Stream.<RawArticleDto>empty())
                            .map(RawArticleDto::newsId)
                        : Stream.<Long>empty(),
                raw.noise() != null
                        ? raw.noise().stream().map(RawArticleDto::newsId)
                        : Stream.<Long>empty()
        ).flatMap(Function.identity()).distinct().toList();

        // news_raw 일괄 조회
        Map<Long, NewsRawEntity> newsMap = newsRawRepository.findAllById(newsIds).stream()
                .collect(Collectors.toMap(NewsRawEntity::getId, Function.identity()));

        // enriched 응답 구성
        TopicDto enrichedTopic = null;
        if (raw.topic() != null) {
            List<ArticleDto> articles = enrichArticles(raw.topic().articles(), newsMap);
            String time = earliestTime(articles);
            enrichedTopic = new TopicDto(raw.topic().title(), raw.topic().summary(), raw.topic().count(), time, articles);
        }

        List<ClusterDto> enrichedClusters = raw.clusters() != null
                ? raw.clusters().stream().map(c -> {
                    List<ArticleDto> articles = enrichArticles(c.articles(), newsMap);
                    String time = earliestTime(articles);
                    return new ClusterDto(c.title(), c.count(), time, articles);
                }).toList()
                : List.of();

        List<ArticleDto> enrichedNoise = enrichArticles(raw.noise(), newsMap);

        // 주가 정보를 DB에서 직접 조회 (클러스터링 스냅샷 대신 최신 주가 사용)
        StockPriceDto stockPrice = toStockPriceDto(priceRepository.findFirstByStockCodeOrderByTrdDdDesc(stockCode));

        RelatedStockDto relatedStock = null;
        if (raw.relatedStock() != null && raw.relatedStock().stockCode() != null) {
            var relPrice = priceRepository.findFirstByStockCodeOrderByTrdDdDesc(raw.relatedStock().stockCode());
            relatedStock = new RelatedStockDto(
                    raw.relatedStock().stockName(), raw.relatedStock().stockCode(), raw.relatedStock().mentionCount(),
                    relPrice != null ? relPrice.getClose() : null,
                    relPrice != null ? relPrice.getDiff() : null,
                    relPrice != null && relPrice.getDiffRate() != null ? relPrice.getDiffRate().doubleValue() : null
            );
        }

        return new StockTopicsDto(
                entity.getStockCode(), entity.getStockName(), raw.totalCount(), stockPrice, relatedStock,
                enrichedTopic, enrichedClusters, enrichedNoise
        );
    }

    private List<ArticleDto> enrichArticles(List<RawArticleDto> rawArticles, Map<Long, NewsRawEntity> newsMap) {
        if (rawArticles == null) return List.of();
        return rawArticles.stream().map(a -> {
            NewsRawEntity news = newsMap.get(a.newsId());
            if (news != null) {
                return new ArticleDto(a.newsId(), a.title(), news.getPress(), news.getUrl(), news.getPublishedAt());
            }
            return new ArticleDto(a.newsId(), a.title(), null, null, null);
        }).toList();
    }

    private StockPriceDto toStockPriceDto(StockPriceDailyRawEntity price) {
        if (price == null) return null;
        return new StockPriceDto(
                price.getStockCode(),
                price.getTrdDd().toString(),
                price.getClose(),
                price.getDiff(),
                price.getDiffRate() != null ? price.getDiffRate().doubleValue() : null
        );
    }

    /** articles 중 가장 빠른 publishedAt을 HH:mm 포맷으로 반환 */
    private String earliestTime(List<ArticleDto> articles) {
        return articles.stream()
                .map(ArticleDto::publishedAt)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .map(TIME_FORMATTER::format)
                .orElse(null);
    }
}
