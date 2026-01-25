package com.hanzi.stocker.ingest.news.engine;

import com.hanzi.stocker.ingest.news.model.FetchResult;
import com.hanzi.stocker.ingest.news.model.ParsedArticle;
import com.hanzi.stocker.ingest.news.model.SitemapEntry;
import com.hanzi.stocker.ingest.news.provider.NewsProvider;
import com.hanzi.stocker.ingest.news.raw.NewsRawService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class NewsCrawlEngine {

    private static final Logger crawlLog = LoggerFactory.getLogger("CRAWL");

    private final RobotsService robotsService;
    private final SitemapService sitemapService;
    private final HttpFetcher httpFetcher;
    private final NewsRawService newsRawService;
    private final CrawlConfig config;

    public NewsCrawlEngine(
            RobotsService robotsService,
            SitemapService sitemapService,
            HttpFetcher httpFetcher,
            NewsRawService newsRawService,
            CrawlConfig config) {
        this.robotsService = robotsService;
        this.sitemapService = sitemapService;
        this.httpFetcher = httpFetcher;
        this.newsRawService = newsRawService;
        this.config = config;
    }

    public CrawlResult crawl(NewsProvider provider, String jobId) {
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        crawlLog.info("event=PROVIDER_START jobId={} provider={}", jobId, provider.id());

        // Step 1: Fetch robots.txt
        RobotsService.RobotsPolicy robotsPolicy = robotsService.fetch(
                provider.baseUrl(),
                config.getUserAgent()
        );

        // Step 2: Determine sitemap URLs
        List<String> sitemapUrls = determineSitemapUrls(robotsPolicy, provider);
        if (sitemapUrls.isEmpty()) {
            crawlLog.warn("event=PROVIDER_SKIPPED jobId={} provider={} reason=NO_SITEMAP", jobId, provider.id());
            return new CrawlResult(0, 0, 0, 0, "No sitemap found");
        }

        // Step 3: Try sitemaps until we find articles
        List<SitemapEntry> entries = new ArrayList<>();

        for (String sitemapUrl : sitemapUrls) {
            Optional<List<SitemapEntry>> entriesOpt = sitemapService.fetch(sitemapUrl, config.getUserAgent());

            if (entriesOpt.isPresent() && !entriesOpt.get().isEmpty()) {
                List<SitemapEntry> filtered = entriesOpt.get().stream()
                        .filter(e -> provider.isArticleUrl(e.loc()))
                        .toList();

                if (!filtered.isEmpty()) {
                    entries = new ArrayList<>(filtered);
                    break;
                }
            }
        }

        if (entries.isEmpty()) {
            crawlLog.warn("event=PROVIDER_SKIPPED jobId={} provider={} reason=NO_ARTICLES_IN_SITEMAP", jobId, provider.id());
            return new CrawlResult(0, 0, 0, 0, "No articles in sitemap");
        }

        // Step 4: Limit article URLs
        List<SitemapEntry> articleEntries = entries.stream()
                .limit(config.getMaxArticlesPerProvider())
                .toList();

        int fetched = articleEntries.size();

        // Step 5: Process each article
        for (int i = 0; i < articleEntries.size(); i++) {
            SitemapEntry entry = articleEntries.get(i);
            String articleUrl = entry.loc();

            // Check robots.txt
            String path = URI.create(articleUrl).getPath();
            if (!robotsPolicy.isAllowed(path)) {
                crawlLog.warn("event=ARTICLE_SKIPPED jobId={} provider={} reason=ROBOTS_DISALLOWED", jobId, provider.id());
                skipCount++;
                continue;
            }

            // Rate limiting
            if (i > 0) {
                sleep(config.getDelaySeconds());
            }

            // Fetch article
            FetchResult fetchResult = httpFetcher.fetch(articleUrl, config.getUserAgent());

            if (fetchResult.isRateLimited()) {
                crawlLog.warn("event=PROVIDER_SKIPPED jobId={} provider={} reason=HTTP_429 action=SKIP_PROVIDER", jobId, provider.id());
                return new CrawlResult(fetched, successCount, failCount, skipCount, "Rate limited");
            }

            if (!fetchResult.isSuccess()) {
                crawlLog.warn("event=ARTICLE_SKIPPED jobId={} provider={} reason=FETCH_FAILED status={}", jobId, provider.id(), fetchResult.statusCode());
                failCount++;
                continue;
            }

            // Parse article
            try {
                ParsedArticle article = provider.parseArticle(fetchResult.body(), articleUrl);

                if (article == null || article.rawText() == null || article.rawText().isBlank()) {
                    crawlLog.warn("event=ARTICLE_SKIPPED jobId={} provider={} reason=EMPTY_CONTENT", jobId, provider.id());
                    skipCount++;
                    continue;
                }

                // Save
                boolean saved = newsRawService.save(provider.id(), article, articleUrl);
                if (saved) {
                    successCount++;
                } else {
                    crawlLog.warn("event=ARTICLE_SKIPPED jobId={} provider={} reason=DUPLICATE", jobId, provider.id());
                    skipCount++;
                }

            } catch (Exception e) {
                crawlLog.warn("event=ARTICLE_SKIPPED jobId={} provider={} reason=PARSE_FAILED", jobId, provider.id());
                failCount++;
            }
        }

        crawlLog.info("event=PROVIDER_SUCCESS jobId={} provider={} fetched={} saved={} failed={} skipped={}",
                jobId, provider.id(), fetched, successCount, failCount, skipCount);

        return new CrawlResult(fetched, successCount, failCount, skipCount, null);
    }

    private List<String> determineSitemapUrls(RobotsService.RobotsPolicy robotsPolicy, NewsProvider provider) {
        List<String> candidates = new ArrayList<>();

        // Priority 1: Provider hints (provider knows their site best)
        List<String> hints = provider.sitemapHints();
        if (hints != null) {
            candidates.addAll(hints);
        }

        // Priority 2: All robots.txt sitemaps as fallback (no filtering)
        for (String url : robotsPolicy.sitemapUrls()) {
            if (!candidates.contains(url)) {
                candidates.add(url);
            }
        }

        return candidates;
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public record CrawlResult(int fetchedCount, int successCount, int failCount, int skipCount, String error) {
        public boolean hasError() {
            return error != null;
        }
    }
}
