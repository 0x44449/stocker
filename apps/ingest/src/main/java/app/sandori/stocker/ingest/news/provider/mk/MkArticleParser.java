package app.sandori.stocker.ingest.news.provider.mk;

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
public class MkArticleParser {

    private static final String PRESS_NAME = "매일경제";

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

        // 2. h1.top_title
        Element h1 = doc.selectFirst("h1.top_title");
        if (h1 != null) {
            return h1.text();
        }

        // 3. fallback to title tag
        return doc.title();
    }

    private String extractRawText(Document doc) {
        // 매일경제 본문 selector: div.news_cnt_detail_wrap
        Element article = doc.selectFirst("div.news_cnt_detail_wrap");

        if (article == null) {
            // fallback: article tag
            article = doc.selectFirst("article");
        }

        if (article == null) {
            return null;
        }

        // 불필요한 요소 제거
        article.select("script, style, iframe, .ad, .advertisement, .reporter_info, .copyright, figure, .relation_wrap").remove();

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

        // 3. 매일경제 특정 클래스
        Element dateElement = doc.selectFirst(".time_area time, .article_time");
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
