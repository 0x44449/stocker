package com.hanzi.stocker.api.feed;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * 뉴스량 이상 감지 서비스.
 * Python analyzer의 이상 탐지 API를 호출하여 결과를 그대로 반환한다.
 */
@Service
public class NewsAnomalyService {

    private final RestClient restClient;

    public NewsAnomalyService(@Value("${analyzer.url}") String analyzerUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(analyzerUrl)
                .build();
    }

    // --- DTO ---

    public record AnomalyItem(
            @JsonProperty("stock_code") String stockCode,
            @JsonProperty("stock_name") String stockName,
            @JsonProperty("today_count") int todayCount,
            @JsonProperty("avg_count") double avgCount,
            double ratio
    ) {}

    public record AnomalyResponse(List<AnomalyItem> items) {}

    public AnomalyResponse detect() {
        return restClient.get()
                .uri("/anomaly/detect")
                .retrieve()
                .body(AnomalyResponse.class);
    }
}
