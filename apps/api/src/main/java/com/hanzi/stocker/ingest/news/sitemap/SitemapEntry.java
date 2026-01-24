package com.hanzi.stocker.ingest.news.sitemap;

import java.time.LocalDateTime;

public class SitemapEntry {

    private final String loc;
    private final LocalDateTime lastModified;

    public SitemapEntry(String loc, LocalDateTime lastModified) {
        this.loc = loc;
        this.lastModified = lastModified;
    }

    public String getLoc() {
        return loc;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }
}
