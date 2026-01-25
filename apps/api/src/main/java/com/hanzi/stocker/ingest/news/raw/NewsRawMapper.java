package com.hanzi.stocker.ingest.news.raw;

import com.hanzi.stocker.ingest.news.model.ParsedArticle;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NewsRawMapper {

    public NewsRawEntity toEntity(
            String source,
            ParsedArticle article,
            String url,
            LocalDateTime collectedAt,
            LocalDateTime expiresAt,
            int maxRawTextLength) {

        String rawText = article.rawText();
        if (rawText != null && rawText.length() > maxRawTextLength) {
            rawText = rawText.substring(0, maxRawTextLength);
        }

        NewsRawEntity entity = new NewsRawEntity();
        entity.setSource(source);
        entity.setPress(article.press());
        entity.setTitle(article.title());
        entity.setRawText(rawText);
        entity.setUrl(url);
        entity.setPublishedAt(article.publishedAt());
        entity.setCollectedAt(collectedAt);
        entity.setExpiresAt(expiresAt);
        return entity;
    }
}
