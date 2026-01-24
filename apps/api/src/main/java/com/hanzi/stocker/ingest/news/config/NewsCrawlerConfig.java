package com.hanzi.stocker.ingest.news.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "crawler")
public class NewsCrawlerConfig {

    private String userAgent = "StockerBot/1.0 (news-crawler)";
    private int maxArticles = 10;
    private int crawlDelaySeconds = 2;
    private int rawTextMaxLength = 2000;
    private int rawRetentionDays = 30;

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getMaxArticles() {
        return maxArticles;
    }

    public void setMaxArticles(int maxArticles) {
        this.maxArticles = maxArticles;
    }

    public int getCrawlDelaySeconds() {
        return crawlDelaySeconds;
    }

    public void setCrawlDelaySeconds(int crawlDelaySeconds) {
        this.crawlDelaySeconds = crawlDelaySeconds;
    }

    public int getRawTextMaxLength() {
        return rawTextMaxLength;
    }

    public void setRawTextMaxLength(int rawTextMaxLength) {
        this.rawTextMaxLength = rawTextMaxLength;
    }

    public int getRawRetentionDays() {
        return rawRetentionDays;
    }

    public void setRawRetentionDays(int rawRetentionDays) {
        this.rawRetentionDays = rawRetentionDays;
    }
}
