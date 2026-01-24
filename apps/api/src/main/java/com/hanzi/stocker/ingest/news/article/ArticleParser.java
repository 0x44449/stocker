package com.hanzi.stocker.ingest.news.article;

import com.hanzi.stocker.ingest.news.config.NewsCrawlerConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class ArticleParser {

    private final NewsCrawlerConfig config;

    public ArticleParser(NewsCrawlerConfig config) {
        this.config = config;
    }

    public ParsedArticle parse(String html, String url, String press, String articleSelector) {
        Document doc = Jsoup.parse(html);

        String title = extractTitle(doc);
        String rawText = extractRawText(doc, articleSelector);
        LocalDateTime publishedAt = extractPublishedAt(doc);

        if (rawText != null && rawText.length() > config.getRawTextMaxLength()) {
            rawText = rawText.substring(0, config.getRawTextMaxLength());
        }

        return new ParsedArticle(title, rawText, url, press, publishedAt);
    }

    private String extractTitle(Document doc) {
        Element titleElement = doc.selectFirst("meta[property=og:title]");
        if (titleElement != null) {
            return titleElement.attr("content");
        }

        Element h1 = doc.selectFirst("h1");
        if (h1 != null) {
            return h1.text();
        }

        return doc.title();
    }

    private String extractRawText(Document doc, String articleSelector) {
        Element article = doc.selectFirst(articleSelector);
        if (article == null) {
            article = doc.selectFirst("article");
        }
        if (article == null) {
            article = doc.selectFirst("[class*=article], [class*=content], [id*=article], [id*=content]");
        }

        if (article != null) {
            article.select("script, style, iframe, .ad, .advertisement, .comment, .comments").remove();
            return article.text();
        }

        return null;
    }

    private LocalDateTime extractPublishedAt(Document doc) {
        Element metaTime = doc.selectFirst("meta[property=article:published_time]");
        if (metaTime != null) {
            return parseDateTime(metaTime.attr("content"));
        }

        Element timeElement = doc.selectFirst("time[datetime]");
        if (timeElement != null) {
            return parseDateTime(timeElement.attr("datetime"));
        }

        return null;
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            try {
                return java.time.OffsetDateTime.parse(dateStr).toLocalDateTime();
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
}
