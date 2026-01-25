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

    private static final Logger log = LoggerFactory.getLogger(NewsCrawlEngine.class);

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

    public CrawlResult crawl(NewsProvider provider) {
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        log.info("Starting crawl for provider: {}", provider.id());

        // Step 1: Fetch robots.txt
        RobotsService.RobotsPolicy robotsPolicy = robotsService.fetch(
                provider.baseUrl(),
                config.getUserAgent()
        );
        log.info("Loaded robots.txt: {} disallow rules", robotsPolicy.disallowedPaths().size());

        // Step 2: Determine sitemap URLs
        List<String> sitemapUrls = determineSitemapUrls(robotsPolicy, provider);
        if (sitemapUrls.isEmpty()) {
            log.warn("No sitemap found for provider: {}", provider.id());
            return new CrawlResult(0, 0, 0, "No sitemap found");
        }
        log.info("Found {} sitemap candidates", sitemapUrls.size());

        // Step 3: Try sitemaps until we find articles
        List<SitemapEntry> entries = new ArrayList<>();
        String usedSitemap = null;

        for (String sitemapUrl : sitemapUrls) {
            log.debug("Trying sitemap: {}", sitemapUrl);
            Optional<List<SitemapEntry>> entriesOpt = sitemapService.fetch(sitemapUrl, config.getUserAgent());

            if (entriesOpt.isPresent() && !entriesOpt.get().isEmpty()) {
                List<SitemapEntry> filtered = entriesOpt.get().stream()
                        .filter(e -> provider.isArticleUrl(e.loc()))
                        .toList();

                if (!filtered.isEmpty()) {
                    entries = new ArrayList<>(filtered);
                    usedSitemap = sitemapUrl;
                    break;
                }
            }
        }

        if (entries.isEmpty()) {
            log.warn("No article URLs found in any sitemap for provider: {}", provider.id());
            return new CrawlResult(0, 0, 0, "No articles in sitemap");
        }

        log.info("Using sitemap: {} with {} entries", usedSitemap, entries.size());

        // Step 4: Limit article URLs
        List<SitemapEntry> articleEntries = entries.stream()
                .limit(config.getMaxArticlesPerProvider())
                .toList();
        log.info("Processing {} article URLs", articleEntries.size());

        // Step 5: Process each article
        for (int i = 0; i < articleEntries.size(); i++) {
            SitemapEntry entry = articleEntries.get(i);
            String articleUrl = entry.loc();

            // Check robots.txt
            String path = URI.create(articleUrl).getPath();
            if (!robotsPolicy.isAllowed(path)) {
                log.debug("Skipping disallowed URL: {}", articleUrl);
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
                log.warn("Rate limited at URL: {}, stopping provider crawl", articleUrl);
                return new CrawlResult(successCount, failCount, skipCount, "Rate limited");
            }

            if (!fetchResult.isSuccess()) {
                log.debug("Failed to fetch article: {} (status={})", articleUrl, fetchResult.statusCode());
                failCount++;
                continue;
            }

            // Parse article
            try {
                ParsedArticle article = provider.parseArticle(fetchResult.body(), articleUrl);

                if (article == null || article.rawText() == null || article.rawText().isBlank()) {
                    log.debug("Empty article content: {}", articleUrl);
                    skipCount++;
                    continue;
                }

                // Save
                boolean saved = newsRawService.save(provider.id(), article, articleUrl);
                if (saved) {
                    successCount++;
                    log.debug("Saved article: {}", article.title());
                } else {
                    skipCount++;
                    log.debug("Skipped article (duplicate): {}", articleUrl);
                }

            } catch (Exception e) {
                log.warn("Failed to parse article {}: {}", articleUrl, e.getMessage());
                failCount++;
            }
        }

        log.info("Crawl completed for {}: success={}, fail={}, skip={}",
                provider.id(), successCount, failCount, skipCount);

        return new CrawlResult(successCount, failCount, skipCount, null);
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

    public record CrawlResult(int successCount, int failCount, int skipCount, String error) {
        public boolean hasError() {
            return error != null;
        }
    }
}
