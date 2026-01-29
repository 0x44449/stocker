package com.hanzi.stocker.ingest.news;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "crawler.news")
public class NewsCrawlConfig {

    private String userAgent = "StockerBot/1.0";
    private int rawTextMaxLength = 2000;
    private int rawRetentionDays = 30;

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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
