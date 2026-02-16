package app.sandori.stocker.api.ingest.news.provider.etoday;

import app.sandori.stocker.api.ingest.news.provider.ParsedArticle;
import app.sandori.stocker.api.ingest.news.provider.NewsProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EtodayProvider implements NewsProvider {

    private static final String ID = "etoday";
    private static final String BASE_URL = "https://www.etoday.co.kr";
    private static final String PRESS = "이투데이";

    private final EtodayArticleParser articleParser;
    private final EtodayUrlPolicy urlPolicy;

    public EtodayProvider(EtodayArticleParser articleParser, EtodayUrlPolicy urlPolicy) {
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
                "https://www.etoday.co.kr/rss/news_sitemap"
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
