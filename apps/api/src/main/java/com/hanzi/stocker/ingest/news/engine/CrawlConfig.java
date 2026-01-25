package com.hanzi.stocker.ingest.news.engine;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "crawler")
public class CrawlConfig {

    private String userAgent = "StockerBot/1.0 (news-crawler)";
    private int delaySeconds = 2;
    private int maxArticlesPerProvider = 10;
    private int rawTextMaxLength = 2000;
    private int rawRetentionDays = 30;

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getDelaySeconds() {
        return delaySeconds;
    }

    public void setDelaySeconds(int delaySeconds) {
        this.delaySeconds = delaySeconds;
    }

    public int getMaxArticlesPerProvider() {
        return maxArticlesPerProvider;
    }

    public void setMaxArticlesPerProvider(int maxArticlesPerProvider) {
        this.maxArticlesPerProvider = maxArticlesPerProvider;
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
