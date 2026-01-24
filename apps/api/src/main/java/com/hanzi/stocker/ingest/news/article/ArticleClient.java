package com.hanzi.stocker.ingest.news.article;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class ArticleClient {

    private final HttpClient httpClient;

    public ArticleClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public String fetch(String articleUrl, String userAgent) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(articleUrl))
                .header("User-Agent", userAgent)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        if (statusCode == 403 || statusCode == 429) {
            throw new RateLimitException("Rate limited: HTTP " + statusCode);
        }

        if (statusCode != 200) {
            throw new IOException("Failed to fetch article: HTTP " + statusCode);
        }

        return response.body();
    }

    public static class RateLimitException extends IOException {
        public RateLimitException(String message) {
            super(message);
        }
    }
}
