package com.hanzi.stocker.ingest.news.raw;

import com.hanzi.stocker.ingest.news.article.ParsedArticle;
import com.hanzi.stocker.ingest.news.crawler.NewsCrawlContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NewsRawMapper {

    public NewsRawEntity toEntity(
            ParsedArticle article,
            NewsCrawlContext context,
            LocalDateTime collectedAt,
            LocalDateTime expiresAt) {

        NewsRawEntity entity = new NewsRawEntity();
        entity.setSource(context.getSource());
        entity.setPress(article.getPress());
        entity.setTitle(article.getTitle());
        entity.setRawText(article.getRawText());
        entity.setUrl(article.getUrl());
        entity.setPublishedAt(article.getPublishedAt());
        entity.setCollectedAt(collectedAt);
        entity.setExpiresAt(expiresAt);
        return entity;
    }
}
