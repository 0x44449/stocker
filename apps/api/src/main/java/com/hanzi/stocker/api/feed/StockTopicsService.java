package com.hanzi.stocker.api.feed;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanzi.stocker.entities.StockClusterResultEntity;
import com.hanzi.stocker.repositories.StockClusterResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 종목별 뉴스 클러스터링 서비스.
 * DB에 저장된 클러스터링 결과를 조회하여 반환한다.
 */
@Service
public class StockTopicsService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final StockClusterResultRepository repository;

    public StockTopicsService(StockClusterResultRepository repository) {
        this.repository = repository;
    }

    // --- DTO ---

    public record ArticleDto(@JsonProperty("news_id") long newsId, String title) {}

    public record StockPriceDto(
            @JsonProperty("stock_code") String stockCode,
            String date,
            Long close,
            @JsonProperty("diff_rate") Double diffRate
    ) {}

    public record RelatedStockDto(
            @JsonProperty("stock_name") String stockName,
            @JsonProperty("stock_code") String stockCode,
            @JsonProperty("mention_count") int mentionCount,
            Long close,
            @JsonProperty("diff_rate") Double diffRate
    ) {}

    public record TopicDto(String title, String summary, int count, List<ArticleDto> articles) {}

    public record ClusterDto(int count, List<ArticleDto> articles) {}

    public record StockTopicsDto(
            String keyword,
            @JsonProperty("total_count") int totalCount,
            @JsonProperty("stock_price") StockPriceDto stockPrice,
            @JsonProperty("related_stock") RelatedStockDto relatedStock,
            TopicDto topic,
            List<ClusterDto> clusters,
            List<ArticleDto> noise
    ) {}

    public StockTopicsDto getStockTopics(String keyword) {
        StockClusterResultEntity entity = repository.findFirstByStockNameOrderByClusteredAtDesc(keyword);
        if (entity == null) {
            return new StockTopicsDto(keyword, 0, null, null, null, List.of(), List.of());
        }

        try {
            return objectMapper.readValue(entity.getResult(), StockTopicsDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("클러스터링 결과 역직렬화 실패", e);
        }
    }
}
