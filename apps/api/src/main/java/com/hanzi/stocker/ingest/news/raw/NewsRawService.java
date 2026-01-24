package com.hanzi.stocker.ingest.news.raw;

import com.hanzi.stocker.ingest.news.article.ParsedArticle;
import com.hanzi.stocker.ingest.news.config.NewsCrawlerConfig;
import com.hanzi.stocker.ingest.news.crawler.NewsCrawlContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NewsRawService {

    private final NewsRawRepository repository;
    private final NewsRawMapper mapper;
    private final NewsCrawlerConfig config;

    public NewsRawService(NewsRawRepository repository, NewsRawMapper mapper, NewsCrawlerConfig config) {
        this.repository = repository;
        this.mapper = mapper;
        this.config = config;
    }

    public boolean save(ParsedArticle article, NewsCrawlContext context) {
        if (article.getRawText() == null || article.getRawText().isBlank()) {
            return false;
        }

        if (article.getRawText().length() > config.getRawTextMaxLength()) {
            return false;
        }

        if (repository.existsByUrl(article.getUrl())) {
            return false;
        }

        LocalDateTime collectedAt = LocalDateTime.now();
        LocalDateTime expiresAt = collectedAt.plusDays(config.getRawRetentionDays());

        NewsRawEntity entity = mapper.toEntity(article, context, collectedAt, expiresAt);
        repository.save(entity);
        return true;
    }
}
