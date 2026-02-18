package app.sandori.stocker.ingest.news.provider.mk;

import app.sandori.stocker.ingest.news.provider.ParsedArticle;
import app.sandori.stocker.ingest.news.provider.NewsProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MkProvider implements NewsProvider {

    private static final String ID = "mk";
    private static final String BASE_URL = "https://www.mk.co.kr";
    private static final String PRESS = "매일경제";

    private final MkArticleParser articleParser;
    private final MkUrlPolicy urlPolicy;

    public MkProvider(MkArticleParser articleParser, MkUrlPolicy urlPolicy) {
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
                BASE_URL + "/sitemap/latest-articles/"
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
