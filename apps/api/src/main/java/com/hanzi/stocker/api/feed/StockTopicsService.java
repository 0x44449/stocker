package com.hanzi.stocker.api.feed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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

    public Map getStockTopics(String keyword, int days, double eps) {
        return restClient.post()
                .uri("/clustering/similar-news")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("keyword", keyword, "days", days, "eps", eps))
                .retrieve()
                .body(Map.class);
    }
}
