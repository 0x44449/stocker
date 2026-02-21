package app.sandori.stocker.ingest.news.provider.etoday;

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
public class EtodayArticleParser {

    private static final String PRESS_NAME = "이투데이";

    public ParsedArticle parse(String html, String url) {
        Document doc = Jsoup.parse(html);

        String title = extractTitle(doc);
        String imageUrl = extractImageUrl(doc);
        // extractRawText가 DOM을 변경하므로 이미지 추출 후 호출
        String rawText = extractRawText(doc);
        LocalDateTime publishedAt = extractPublishedAt(doc);

        if (rawText == null || rawText.isBlank()) {
            return null;
        }

        return new ParsedArticle(title, rawText, PRESS_NAME, publishedAt, imageUrl);
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

        // 2. h1.main_title
        Element h1 = doc.selectFirst("h1.main_title");
        if (h1 != null) {
            return h1.text();
        }

        return doc.title();
    }

    private String extractImageUrl(Document doc) {
        // 1. og:image
        Element ogImage = doc.selectFirst("meta[property=og:image]");
        if (ogImage != null) {
            String content = ogImage.attr("content");
            if (!content.isBlank()) {
                return content;
            }
        }

        // 2. twitter:image
        Element twitterImage = doc.selectFirst("meta[name=twitter:image], meta[property=twitter:image]");
        if (twitterImage != null) {
            String content = twitterImage.attr("content");
            if (!content.isBlank()) {
                return content;
            }
        }

        // 3. 본문 영역 첫 img
        Element article = doc.selectFirst("div.articleView[itemprop=articleBody]");
        if (article != null) {
            Element img = article.selectFirst("img[src]");
            if (img != null) {
                String src = img.attr("abs:src");
                if (!src.isBlank()) {
                    return src;
                }
            }
        }

        return null;
    }

    private String extractRawText(Document doc) {
        // 이투데이 본문: div.articleView[itemprop=articleBody]
        Element article = doc.selectFirst("div.articleView[itemprop=articleBody]");

        if (article == null) {
            return null;
        }

        // 불필요한 요소 제거
        article.select(
                "script, style, iframe, " +
                ".img_box_C, " +
                "[id^=div-gpt-ad], " +
                ".relation_newslist, " +
                ".kwd_tags, " +
                ".card-container, " +
                ".reporter_topNews, " +
                ".recommend_btn, " +
                ".ico_share"
        ).remove();

        return article.text();
    }

    private LocalDateTime extractPublishedAt(Document doc) {
        // meta article:published_time (OffsetDateTime 형식: 2026-02-12T17:05:00+09:00)
        Element metaTime = doc.selectFirst("meta[property=article:published_time]");
        if (metaTime != null) {
            LocalDateTime parsed = parseDateTime(metaTime.attr("content"));
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
