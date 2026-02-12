package com.hanzi.stocker.api.feed;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * 종목별 뉴스 클러스터링 서비스.
 * Python analyzer의 클러스터링 API를 호출하여 결과를 그대로 반환한다.
 */
@Service
public class StockTopicsService {

    private final RestClient restClient;

    public StockTopicsService(@Value("${analyzer.url}") String analyzerUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(analyzerUrl)
                .build();
    }

    // --- DTO ---

    public record ArticleDto(@JsonProperty("news_id") long newsId, String title) {}

    public record TopicDto(String title, String summary, int count, List<ArticleDto> articles) {}

    public record ClusterDto(int count, List<ArticleDto> articles) {}

    public record StockTopicsDto(
            String keyword,
            @JsonProperty("total_count") int totalCount,
            TopicDto topic,
            List<ClusterDto> clusters,
            List<ArticleDto> noise
    ) {}

    public StockTopicsDto getStockTopics(String keyword, int days, double eps) {
        return restClient.post()
                .uri("/clustering/similar-news")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("keyword", keyword, "days", days, "eps", eps))
                .retrieve()
                .body(StockTopicsDto.class);
    }
}
