package com.hanzi.stocker.ingest.news.engine;

import com.hanzi.stocker.ingest.news.model.FetchResult;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class HttpFetcher {

    private final HttpClient httpClient;

    public HttpFetcher() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public FetchResult fetch(String url, String userAgent) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", userAgent)
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            if (statusCode == 403 || statusCode == 429) {
                return FetchResult.rateLimited(statusCode);
            }

            if (statusCode != 200) {
                return FetchResult.error(statusCode);
            }

            return FetchResult.success(response.body());

        } catch (Exception e) {
            return FetchResult.error(-1);
        }
    }
}
