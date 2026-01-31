package com.hanzi.stocker.api.internal;

import com.hanzi.stocker.ingest.news.NewsCrawlEngine;
import com.hanzi.stocker.ingest.news.NewsCrawlLock;
import com.hanzi.stocker.ingest.news.provider.ProviderRegistry;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/internal/crawl/news")
public class NewsCrawlController {

    private final NewsCrawlEngine engine;
    private final ProviderRegistry providerRegistry;
    private final NewsCrawlLock crawlLock;

    public NewsCrawlController(NewsCrawlEngine engine, ProviderRegistry providerRegistry, NewsCrawlLock crawlLock) {
        this.engine = engine;
        this.providerRegistry = providerRegistry;
        this.crawlLock = crawlLock;
    }

    public record CrawlTriggerResponse(String status, String provider) {}

    public record CrawlStatusResponse(String provider, String status) {}

    /** 특정 프로바이더 크롤링을 수동으로 트리거한다. 비동기 실행 후 즉시 응답. */
    @PostMapping("/{providerId}")
    public CrawlTriggerResponse trigger(@PathVariable String providerId) {
        var provider = providerRegistry.get(providerId);
        if (provider.isEmpty()) {
            return new CrawlTriggerResponse("not_found", providerId);
        }

        if (!crawlLock.tryLock(providerId)) {
            return new CrawlTriggerResponse("already_running", providerId);
        }

        CompletableFuture.runAsync(() -> {
            try {
                engine.crawl(provider.get());
            } finally {
                crawlLock.unlock(providerId);
            }
        });

        return new CrawlTriggerResponse("started", providerId);
    }

    /** 프로바이더별 크롤링 상태를 조회한다. */
    @GetMapping("/status")
    public List<CrawlStatusResponse> status() {
        return providerRegistry.getAllIds().stream()
                .map(id -> new CrawlStatusResponse(id, crawlLock.isRunning(id) ? "running" : "idle"))
                .toList();
    }
}
