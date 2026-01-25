package com.hanzi.stocker.ingest.news.model;

import java.time.LocalDateTime;

public record SitemapEntry(
        String loc,
        LocalDateTime lastModified
) {}
