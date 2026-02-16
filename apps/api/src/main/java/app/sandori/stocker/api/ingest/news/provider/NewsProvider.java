package app.sandori.stocker.api.ingest.news.provider;

import java.util.List;

public interface NewsProvider {

    String id();

    String baseUrl();

    String press();

    List<String> sitemapHints();

    boolean isArticleUrl(String url);

    ParsedArticle parseArticle(String html, String url);
}
