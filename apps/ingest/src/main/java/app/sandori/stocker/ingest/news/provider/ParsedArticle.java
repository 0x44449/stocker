package app.sandori.stocker.ingest.news.provider;

import java.time.LocalDateTime;

public record ParsedArticle(
        String title,
        String rawText,
        String press,
        LocalDateTime publishedAt,
        String imageUrl
) {}
