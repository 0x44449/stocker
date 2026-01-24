package com.hanzi.stocker.ingest.news.crawler;

import com.hanzi.stocker.ingest.news.article.ArticleClient;
import com.hanzi.stocker.ingest.news.article.ArticleParser;
import com.hanzi.stocker.ingest.news.article.ParsedArticle;
import com.hanzi.stocker.ingest.news.raw.NewsRawService;
import com.hanzi.stocker.ingest.news.sitemap.SitemapClient;
import com.hanzi.stocker.ingest.news.sitemap.SitemapEntry;
import com.hanzi.stocker.ingest.news.sitemap.SitemapParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Component
public class NewsCrawler {

    private static final Logger log = LoggerFactory.getLogger(NewsCrawler.class);

    private final RobotsTxtPolicy robotsTxtPolicy;
    private final SitemapClient sitemapClient;
    private final SitemapParser sitemapParser;
    private final ArticleClient articleClient;
    private final ArticleParser articleParser;
    private final NewsRawService newsRawService;

    public NewsCrawler(
            RobotsTxtPolicy robotsTxtPolicy,
            SitemapClient sitemapClient,
            SitemapParser sitemapParser,
            ArticleClient articleClient,
            ArticleParser articleParser,
            NewsRawService newsRawService) {
        this.robotsTxtPolicy = robotsTxtPolicy;
        this.sitemapClient = sitemapClient;
        this.sitemapParser = sitemapParser;
        this.articleClient = articleClient;
        this.articleParser = articleParser;
        this.newsRawService = newsRawService;
    }

    public CrawlResult crawl(NewsCrawlContext context) {
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        try {
            log.info("Starting crawl for source: {}", context.getSource());

            RobotsTxtPolicy.DisallowRules rules = robotsTxtPolicy.fetch(
                    context.getRobotsTxtUrl(),
                    context.getUserAgent()
            );
            log.info("Loaded robots.txt with {} disallow rules", rules.getDisallowedPaths().size());

            String sitemapXml = sitemapClient.fetch(
                    context.getSitemapUrl(),
                    context.getUserAgent()
            );
            List<SitemapEntry> entries = sitemapParser.parse(sitemapXml);
            log.info("Found {} entries in sitemap", entries.size());

            int limit = Math.min(entries.size(), context.getMaxArticles());

            for (int i = 0; i < limit; i++) {
                SitemapEntry entry = entries.get(i);
                String articleUrl = entry.getLoc();
                String path = URI.create(articleUrl).getPath();

                if (!rules.isAllowed(path)) {
                    log.debug("Skipping disallowed URL: {}", articleUrl);
                    skipCount++;
                    continue;
                }

                try {
                    if (i > 0) {
                        Thread.sleep(context.getCrawlDelaySeconds() * 1000L);
                    }

                    String html = articleClient.fetch(articleUrl, context.getUserAgent());
                    ParsedArticle article = articleParser.parse(
                            html,
                            articleUrl,
                            context.getPress(),
                            context.getArticleSelector()
                    );

                    boolean saved = newsRawService.save(article, context);
                    if (saved) {
                        successCount++;
                        log.debug("Saved article: {}", article.getTitle());
                    } else {
                        skipCount++;
                        log.debug("Skipped article (duplicate or empty): {}", articleUrl);
                    }

                } catch (ArticleClient.RateLimitException e) {
                    log.warn("Rate limited, stopping crawl: {}", e.getMessage());
                    break;
                } catch (Exception e) {
                    failCount++;
                    log.warn("Failed to process article {}: {}", articleUrl, e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Crawl failed for source {}: {}", context.getSource(), e.getMessage());
        }

        log.info("Crawl completed for {}: success={}, fail={}, skip={}",
                context.getSource(), successCount, failCount, skipCount);

        return new CrawlResult(successCount, failCount, skipCount);
    }

    public record CrawlResult(int successCount, int failCount, int skipCount) {}
}
