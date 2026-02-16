package app.sandori.stocker.api.ingest.news.provider.hk;

import app.sandori.stocker.api.ingest.news.provider.ParsedArticle;
import app.sandori.stocker.api.ingest.news.provider.NewsProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HkProvider implements NewsProvider {

    private static final String ID = "hk";
    private static final String BASE_URL = "https://www.hankyung.com";
    private static final String PRESS = "한국경제";

    private final HkArticleParser articleParser;
    private final HkUrlPolicy urlPolicy;

    public HkProvider(HkArticleParser articleParser, HkUrlPolicy urlPolicy) {
        this.articleParser = articleParser;
        this.urlPolicy = urlPolicy;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String baseUrl() {
        return BASE_URL;
    }

    @Override
    public String press() {
        return PRESS;
    }

    @Override
    public List<String> sitemapHints() {
        return List.of(
                BASE_URL + "/sitemap/latest-article.xml",
                BASE_URL + "/sitemap/daily-article.xml"
        );
    }

    @Override
    public boolean isArticleUrl(String url) {
        return urlPolicy.isArticleUrl(url);
    }

    @Override
    public ParsedArticle parseArticle(String html, String url) {
        return articleParser.parse(html, url);
    }
}
