package com.hanzi.stocker.ingest.news.model;

import java.time.LocalDateTime;

public record ParsedArticle(
        String title,
        String rawText,
        String press,
        LocalDateTime publishedAt
) {}
