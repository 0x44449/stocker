package com.hanzi.stocker.ingest.news.engine;

import com.hanzi.stocker.ingest.news.provider.NewsProvider;
import com.hanzi.stocker.ingest.news.provider.ProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsCrawlJobService {

    private static final Logger log = LoggerFactory.getLogger(NewsCrawlJobService.class);

    private final NewsCrawlEngine crawlEngine;
    private final ProviderRegistry providerRegistry;
    private final CrawlLock crawlLock;

    public NewsCrawlJobService(
            NewsCrawlEngine crawlEngine,
            ProviderRegistry providerRegistry,
            CrawlLock crawlLock) {
        this.crawlEngine = crawlEngine;
        this.providerRegistry = providerRegistry;
        this.crawlLock = crawlLock;
    }

    public List<ProviderCrawlResult> runAll() {
        if (!crawlLock.tryAcquire()) {
            log.warn("Crawl already in progress, skipping");
            return List.of();
        }

        try {
            log.info("Starting crawl job for all providers");
            List<ProviderCrawlResult> results = providerRegistry.getAll().stream()
                    .map(this::runProvider)
                    .toList();
            log.info("Crawl job completed for all providers");
            return results;
        } finally {
            crawlLock.release();
        }
    }

    public ProviderCrawlResult runSingle(String providerId) {
        if (!crawlLock.tryAcquire()) {
            log.warn("Crawl already in progress, skipping provider: {}", providerId);
            return new ProviderCrawlResult(providerId,
                    new NewsCrawlEngine.CrawlResult(0, 0, 0, "Crawl already in progress"));
        }

        try {
            return providerRegistry.get(providerId)
                    .map(this::runProvider)
                    .orElse(new ProviderCrawlResult(providerId,
                            new NewsCrawlEngine.CrawlResult(0, 0, 0, "Provider not found")));
        } finally {
            crawlLock.release();
        }
    }

    public boolean isRunning() {
        return crawlLock.isRunning();
    }

    private ProviderCrawlResult runProvider(NewsProvider provider) {
        log.info("Running crawl for provider: {}", provider.id());
        NewsCrawlEngine.CrawlResult result = crawlEngine.crawl(provider);
        return new ProviderCrawlResult(provider.id(), result);
    }

    public record ProviderCrawlResult(String providerId, NewsCrawlEngine.CrawlResult result) {}
}
