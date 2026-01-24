package com.hanzi.stocker.ingest.news.article;

import java.time.LocalDateTime;

public class ParsedArticle {

    private final String title;
    private final String rawText;
    private final String url;
    private final String press;
    private final LocalDateTime publishedAt;

    public ParsedArticle(String title, String rawText, String url, String press, LocalDateTime publishedAt) {
        this.title = title;
        this.rawText = rawText;
        this.url = url;
        this.press = press;
        this.publishedAt = publishedAt;
    }

    public String getTitle() {
        return title;
    }

    public String getRawText() {
        return rawText;
    }

    public String getUrl() {
        return url;
    }

    public String getPress() {
        return press;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}
