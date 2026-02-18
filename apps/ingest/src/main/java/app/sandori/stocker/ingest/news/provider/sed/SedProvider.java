package app.sandori.stocker.ingest.news.provider.sed;

import app.sandori.stocker.ingest.news.provider.ParsedArticle;
import app.sandori.stocker.ingest.news.provider.NewsProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SedProvider implements NewsProvider {

    private static final String ID = "sed";
    private static final String BASE_URL = "https://www.sedaily.com";
    private static final String PRESS = "서울경제";

    private final SedArticleParser articleParser;
    private final SedUrlPolicy urlPolicy;

    public SedProvider(SedArticleParser articleParser, SedUrlPolicy urlPolicy) {
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
                "https://www.sedaily.com/sitemap/latestnews"
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
