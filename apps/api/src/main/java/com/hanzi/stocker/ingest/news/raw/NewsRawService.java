package com.hanzi.stocker.ingest.news.raw;

import com.hanzi.stocker.ingest.news.engine.CrawlConfig;
import com.hanzi.stocker.ingest.news.model.ParsedArticle;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NewsRawService {

    private final NewsRawRepository repository;
    private final NewsRawMapper mapper;
    private final CrawlConfig config;

    public NewsRawService(NewsRawRepository repository, NewsRawMapper mapper, CrawlConfig config) {
        this.repository = repository;
        this.mapper = mapper;
        this.config = config;
    }

    public boolean save(String source, ParsedArticle article, String url) {
        if (article.rawText() == null || article.rawText().isBlank()) {
            return false;
        }

        if (repository.existsByUrl(url)) {
            return false;
        }

        LocalDateTime collectedAt = LocalDateTime.now();
        LocalDateTime expiresAt = collectedAt.plusDays(config.getRawRetentionDays());

        NewsRawEntity entity = mapper.toEntity(
                source,
                article,
                url,
                collectedAt,
                expiresAt,
                config.getRawTextMaxLength()
        );

        repository.save(entity);
        return true;
    }
}
