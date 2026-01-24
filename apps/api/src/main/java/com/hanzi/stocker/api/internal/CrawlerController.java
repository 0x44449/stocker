package com.hanzi.stocker.api.internal;

import com.hanzi.stocker.ingest.news.config.NewsCrawlerConfig;
import com.hanzi.stocker.ingest.news.crawler.NewsCrawlContext;
import com.hanzi.stocker.ingest.news.crawler.NewsCrawler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/crawler")
public class CrawlerController {

    private final NewsCrawler newsCrawler;
    private final NewsCrawlerConfig config;

    public CrawlerController(NewsCrawler newsCrawler, NewsCrawlerConfig config) {
        this.newsCrawler = newsCrawler;
        this.config = config;
    }

    @PostMapping("/run")
    public NewsCrawler.CrawlResult run(
            @RequestParam String source,
            @RequestParam String baseUrl,
            @RequestParam String sitemapPath,
            @RequestParam String press,
            @RequestParam(defaultValue = "article") String articleSelector) {

        NewsCrawlContext context = NewsCrawlContext.builder()
                .source(source)
                .baseUrl(baseUrl)
                .sitemapPath(sitemapPath)
                .press(press)
                .articleSelector(articleSelector)
                .userAgent(config.getUserAgent())
                .maxArticles(config.getMaxArticles())
                .crawlDelaySeconds(config.getCrawlDelaySeconds())
                .build();

        return newsCrawler.crawl(context);
    }
}
