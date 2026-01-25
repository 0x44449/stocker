package com.hanzi.stocker.ingest.news.engine;

import com.hanzi.stocker.ingest.news.provider.NewsProvider;
import com.hanzi.stocker.ingest.news.provider.ProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NewsCrawlJobService {

    private static final Logger crawlLog = LoggerFactory.getLogger("CRAWL");
    private static final DateTimeFormatter JOB_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

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
            crawlLog.warn("event=NEWS_CRAWL_SKIPPED reason=ALREADY_RUNNING");
            return List.of();
        }

        String jobId = generateJobId();
        long startMs = System.currentTimeMillis();
        List<NewsProvider> providers = providerRegistry.getAll();

        crawlLog.info("event=NEWS_CRAWL_STARTED jobId={} providers={}", jobId, providers.size());

        try {
            List<ProviderCrawlResult> results = providers.stream()
                    .map(provider -> runProvider(provider, jobId))
                    .toList();

            long durationMs = System.currentTimeMillis() - startMs;
            int totalFetched = results.stream().mapToInt(r -> r.result().fetchedCount()).sum();
            int totalSaved = results.stream().mapToInt(r -> r.result().successCount()).sum();
            int totalFailed = results.stream().mapToInt(r -> r.result().failCount()).sum();
            int totalSkipped = results.stream().mapToInt(r -> r.result().skipCount()).sum();

            crawlLog.info("event=NEWS_CRAWL_FINISHED jobId={} durationMs={} providers={} fetched={} saved={} failed={} skipped={}",
                    jobId, durationMs, providers.size(), totalFetched, totalSaved, totalFailed, totalSkipped);

            return results;
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startMs;
            crawlLog.error("event=NEWS_CRAWL_ERROR jobId={} durationMs={} reason={}",
                    jobId, durationMs, e.getClass().getSimpleName(), e);
            return List.of();
        } finally {
            crawlLock.release();
        }
    }

    public ProviderCrawlResult runSingle(String providerId) {
        if (!crawlLock.tryAcquire()) {
            crawlLog.warn("event=NEWS_CRAWL_SKIPPED reason=ALREADY_RUNNING provider={}", providerId);
            return new ProviderCrawlResult(providerId,
                    new NewsCrawlEngine.CrawlResult(0, 0, 0, 0, "Crawl already in progress"));
        }

        String jobId = generateJobId();
        crawlLog.info("event=NEWS_CRAWL_STARTED jobId={} providers=1", jobId);

        try {
            return providerRegistry.get(providerId)
                    .map(provider -> runProvider(provider, jobId))
                    .orElseGet(() -> {
                        crawlLog.warn("event=PROVIDER_SKIPPED jobId={} provider={} reason=NOT_FOUND", jobId, providerId);
                        return new ProviderCrawlResult(providerId,
                                new NewsCrawlEngine.CrawlResult(0, 0, 0, 0, "Provider not found"));
                    });
        } finally {
            crawlLock.release();
        }
    }

    public boolean isRunning() {
        return crawlLock.isRunning();
    }

    private ProviderCrawlResult runProvider(NewsProvider provider, String jobId) {
        NewsCrawlEngine.CrawlResult result = crawlEngine.crawl(provider, jobId);
        return new ProviderCrawlResult(provider.id(), result);
    }

    private String generateJobId() {
        return LocalDateTime.now().format(JOB_ID_FORMAT);
    }

    public record ProviderCrawlResult(String providerId, NewsCrawlEngine.CrawlResult result) {}
}
