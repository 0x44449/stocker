package com.hanzi.stocker.api.internal;

import com.hanzi.stocker.ingest.news.NewsCrawlEngine;
import com.hanzi.stocker.ingest.news.NewsCrawlLock;
import com.hanzi.stocker.ingest.news.provider.ProviderRegistry;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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

    @PostMapping("/{providerId}")
    public Map<String, String> trigger(@PathVariable String providerId) {
        var provider = providerRegistry.get(providerId);
        if (provider.isEmpty()) {
            return Map.of("status", "not_found", "provider", providerId);
        }

        if (!crawlLock.tryLock(providerId)) {
            return Map.of("status", "already_running", "provider", providerId);
        }

        CompletableFuture.runAsync(() -> {
            try {
                engine.crawl(provider.get());
            } finally {
                crawlLock.unlock(providerId);
            }
        });

        return Map.of("status", "started", "provider", providerId);
    }

    @GetMapping("/status")
    public Map<String, String> status() {
        return crawlLock.allStatus(providerRegistry.getAllIds());
    }
}
