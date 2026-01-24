package com.hanzi.stocker.ingest.news.crawler;

import java.time.LocalDateTime;

public class NewsCrawlContext {

    private final String source;
    private final String baseUrl;
    private final String sitemapPath;
    private final String articleSelector;
    private final String press;
    private final String userAgent;
    private final int maxArticles;
    private final int crawlDelaySeconds;
    private final LocalDateTime crawlStartedAt;

    private NewsCrawlContext(Builder builder) {
        this.source = builder.source;
        this.baseUrl = builder.baseUrl;
        this.sitemapPath = builder.sitemapPath;
        this.articleSelector = builder.articleSelector;
        this.press = builder.press;
        this.userAgent = builder.userAgent;
        this.maxArticles = builder.maxArticles;
        this.crawlDelaySeconds = builder.crawlDelaySeconds;
        this.crawlStartedAt = LocalDateTime.now();
    }

    public String getSource() {
        return source;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getSitemapUrl() {
        return baseUrl + sitemapPath;
    }

    public String getRobotsTxtUrl() {
        return baseUrl + "/robots.txt";
    }

    public String getArticleSelector() {
        return articleSelector;
    }

    public String getPress() {
        return press;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public int getMaxArticles() {
        return maxArticles;
    }

    public int getCrawlDelaySeconds() {
        return crawlDelaySeconds;
    }

    public LocalDateTime getCrawlStartedAt() {
        return crawlStartedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String source;
        private String baseUrl;
        private String sitemapPath;
        private String articleSelector;
        private String press;
        private String userAgent;
        private int maxArticles;
        private int crawlDelaySeconds;

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder sitemapPath(String sitemapPath) {
            this.sitemapPath = sitemapPath;
            return this;
        }

        public Builder articleSelector(String articleSelector) {
            this.articleSelector = articleSelector;
            return this;
        }

        public Builder press(String press) {
            this.press = press;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder maxArticles(int maxArticles) {
            this.maxArticles = maxArticles;
            return this;
        }

        public Builder crawlDelaySeconds(int crawlDelaySeconds) {
            this.crawlDelaySeconds = crawlDelaySeconds;
            return this;
        }

        public NewsCrawlContext build() {
            return new NewsCrawlContext(this);
        }
    }
}
