package com.hanzi.stocker.ingest.news;

import com.hanzi.stocker.ingest.news.provider.NewsProvider;
import com.hanzi.stocker.ingest.news.provider.ParsedArticle;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class NewsCrawlEngine {

    private static final int MAX_INDEX_DEPTH = 2;

    private final NewsCrawlConfig config;
    private final NewsRawRepository repository;
    private final RestClient restClient;

    public NewsCrawlEngine(NewsCrawlConfig config, NewsRawRepository repository) {
        this.config = config;
        this.repository = repository;
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, config.getUserAgent())
                .build();
    }

    public void crawl(NewsProvider provider) {
        // 1. sitemap에서 기사 URL 수집
        List<String> articleUrls = new ArrayList<>();
        for (String sitemapUrl : provider.sitemapHints()) {
            List<String> urls = fetchSitemap(sitemapUrl, 0);
            for (String url : urls) {
                if (provider.isArticleUrl(url)) {
                    articleUrls.add(url);
                }
            }
            if (!articleUrls.isEmpty()) {
                break;
            }
        }

        // 2. 각 기사 처리
        for (String url : articleUrls) {
            if (repository.existsByUrl(url)) {
                continue;
            }

            String html = fetchHtml(url);
            if (html == null) {
                continue;
            }

            ParsedArticle article = provider.parseArticle(html, url);
            if (article == null || article.rawText() == null || article.rawText().isBlank()) {
                continue;
            }

            String rawText = article.rawText();
            if (rawText.length() > config.getRawTextMaxLength()) {
                rawText = rawText.substring(0, config.getRawTextMaxLength());
            }

            NewsRawEntity entity = new NewsRawEntity();
            entity.setSource(provider.id());
            entity.setPress(provider.press());
            entity.setTitle(article.title());
            entity.setRawText(rawText);
            entity.setUrl(url);
            entity.setPublishedAt(article.publishedAt());
            entity.setCollectedAt(LocalDateTime.now());
            entity.setExpiresAt(LocalDateTime.now().plusDays(config.getRawRetentionDays()));
            repository.save(entity);
        }
    }

    private List<String> fetchSitemap(String sitemapUrl, int depth) {
        if (depth > MAX_INDEX_DEPTH) {
            return List.of();
        }

        String xml = fetchHtml(sitemapUrl);
        if (xml == null) {
            return List.of();
        }

        List<String> urls = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

            // sitemap index인 경우
            NodeList sitemapNodes = doc.getElementsByTagName("sitemap");
            if (sitemapNodes.getLength() > 0) {
                for (int i = 0; i < sitemapNodes.getLength(); i++) {
                    Element el = (Element) sitemapNodes.item(i);
                    String loc = getElementText(el, "loc");
                    if (loc != null && !loc.isBlank()) {
                        urls.addAll(fetchSitemap(loc, depth + 1));
                    }
                }
                return urls;
            }

            // 일반 sitemap
            NodeList urlNodes = doc.getElementsByTagName("url");
            for (int i = 0; i < urlNodes.getLength(); i++) {
                Element el = (Element) urlNodes.item(i);
                String loc = getElementText(el, "loc");
                if (loc != null && !loc.isBlank()) {
                    urls.add(loc);
                }
            }
        } catch (Exception e) {
            // sitemap 파싱 실패 시 무시
        }

        return urls;
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    private String fetchHtml(String url) {
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            return null;
        }
    }
}
