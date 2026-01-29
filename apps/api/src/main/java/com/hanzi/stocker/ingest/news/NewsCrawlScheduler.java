package com.hanzi.stocker.ingest.news;

import com.hanzi.stocker.ingest.news.provider.ProviderRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class NewsCrawlScheduler {

    private final NewsCrawlEngine engine;
    private final ProviderRegistry providerRegistry;

    public NewsCrawlScheduler(NewsCrawlEngine engine, ProviderRegistry providerRegistry) {
        this.engine = engine;
        this.providerRegistry = providerRegistry;
    }

    @Scheduled(cron = "0 0 9,12,15,18 * * *")
    public void run() {
        var futures = providerRegistry.getAll().stream()
                .map(provider -> CompletableFuture.runAsync(() -> engine.crawl(provider)))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
