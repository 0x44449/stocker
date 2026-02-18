package app.sandori.stocker.ingest.news.provider.hk;

import app.sandori.stocker.ingest.news.provider.ParsedArticle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class HkArticleParser {

    private static final String PRESS_NAME = "한국경제";

    public ParsedArticle parse(String html, String url) {
        Document doc = Jsoup.parse(html);

        String title = extractTitle(doc);
        String rawText = extractRawText(doc);
        LocalDateTime publishedAt = extractPublishedAt(doc);

        if (rawText == null || rawText.isBlank()) {
            return null;
        }

        return new ParsedArticle(title, rawText, PRESS_NAME, publishedAt);
    }

    private String extractTitle(Document doc) {
        // 1. og:title
        Element ogTitle = doc.selectFirst("meta[property=og:title]");
        if (ogTitle != null) {
            String content = ogTitle.attr("content");
            if (!content.isBlank()) {
                return content;
            }
        }

        // 2. h1.headline
        Element h1 = doc.selectFirst("h1.headline, h1.article-title");
        if (h1 != null) {
            return h1.text();
        }

        // 3. fallback to title tag
        return doc.title();
    }

    private String extractRawText(Document doc) {
        // 한국경제 본문 selector
        Element article = doc.selectFirst("div.article-body, div#articletxt, div.article-content");

        if (article == null) {
            article = doc.selectFirst("article");
        }

        if (article == null) {
            return null;
        }

        // 불필요한 요소 제거
        article.select("script, style, iframe, .ad, .advertisement, .reporter, .copyright, figure, .relation, .link-group").remove();

        return article.text();
    }

    private LocalDateTime extractPublishedAt(Document doc) {
        // 1. meta article:published_time
        Element metaTime = doc.selectFirst("meta[property=article:published_time]");
        if (metaTime != null) {
            LocalDateTime parsed = parseDateTime(metaTime.attr("content"));
            if (parsed != null) {
                return parsed;
            }
        }

        // 2. time tag with datetime attribute
        Element timeElement = doc.selectFirst("time[datetime]");
        if (timeElement != null) {
            LocalDateTime parsed = parseDateTime(timeElement.attr("datetime"));
            if (parsed != null) {
                return parsed;
            }
        }

        // 3. 한국경제 특정 클래스
        Element dateElement = doc.selectFirst(".article-timestamp time, .date-published");
        if (dateElement != null) {
            LocalDateTime parsed = parseDateTime(dateElement.attr("datetime"));
            if (parsed != null) {
                return parsed;
            }
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
                return OffsetDateTime.parse(dateStr).toLocalDateTime();
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
}
