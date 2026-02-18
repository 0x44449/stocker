package app.sandori.stocker.ingest.controller;

import app.sandori.stocker.ingest.news.NewsCrawlEngine;
import app.sandori.stocker.ingest.news.NewsCrawlLock;
import app.sandori.stocker.ingest.news.provider.ProviderRegistry;
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

    @GetMapping("/status")
    public List<CrawlStatusResponse> status() {
        return providerRegistry.getAllIds().stream()
                .map(id -> new CrawlStatusResponse(id, crawlLock.isRunning(id) ? "running" : "idle"))
                .toList();
    }
}
