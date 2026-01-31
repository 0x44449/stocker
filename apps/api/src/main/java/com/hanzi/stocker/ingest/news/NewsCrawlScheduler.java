package com.hanzi.stocker.ingest.news;

import com.hanzi.stocker.ingest.news.provider.ProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class NewsCrawlScheduler {

    private static final Logger log = LoggerFactory.getLogger(NewsCrawlScheduler.class);

    private final NewsCrawlEngine engine;
    private final ProviderRegistry providerRegistry;

    public NewsCrawlScheduler(NewsCrawlEngine engine, ProviderRegistry providerRegistry) {
        this.engine = engine;
        this.providerRegistry = providerRegistry;
    }

    @Scheduled(cron = "0 0 9,12,15,18 * * *")
    public void run() {
        var providers = providerRegistry.getAll();
        log.info("뉴스 크롤링 스케줄 시작: provider={}개", providers.size());

        var futures = providers.stream()
                .map(provider -> CompletableFuture.runAsync(() -> engine.crawl(provider)))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("뉴스 크롤링 스케줄 종료");
    }
}
