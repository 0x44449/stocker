package app.sandori.stocker.ingest.news;

import app.sandori.stocker.ingest.entities.NewsRawEntity;
import app.sandori.stocker.ingest.repositories.NewsRawRepository;
import app.sandori.stocker.ingest.news.provider.NewsProvider;
import app.sandori.stocker.ingest.news.provider.ParsedArticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    private static final Logger log = LoggerFactory.getLogger(NewsCrawlEngine.class);
    private static final int MAX_INDEX_DEPTH = 2;

    private final NewsCrawlConfig config;
    private final NewsRawRepository repository;
    private final ImageStorageClient imageStorageClient;
    private final RestClient restClient;

    public NewsCrawlEngine(NewsCrawlConfig config, NewsRawRepository repository, ImageStorageClient imageStorageClient) {
        this.config = config;
        this.repository = repository;
        this.imageStorageClient = imageStorageClient;
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, config.getUserAgent())
                .build();
    }

    public void crawl(NewsProvider provider) {
        log.info("[{}] 크롤링 시작", provider.id());

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

        log.info("[{}] 기사 URL 수집 완료: {}건", provider.id(), articleUrls.size());

        // 2. 각 기사 처리
        int savedCount = 0;
        int skippedCount = 0;
        for (int i = 0; i < articleUrls.size(); i++) {
            String url = articleUrls.get(i);

            if (repository.existsByUrl(url)) {
                log.debug("[{}] 이미 존재하여 스킵: {}", provider.id(), url);
                skippedCount++;
                continue;
            }

            if (i > 0) {
                sleep(config.getDelaySeconds());
            }

            String html = fetchHtml(url);
            if (html == null) {
                log.debug("[{}] HTML 가져오기 실패: {}", provider.id(), url);
                continue;
            }

            ParsedArticle article = provider.parseArticle(html, url);
            if (article == null || article.rawText() == null || article.rawText().isBlank()) {
                log.debug("[{}] 파싱 실패 또는 본문 없음: {}", provider.id(), url);
                continue;
            }

            String rawText = article.rawText();

            // 이미지 다운로드 → MinIO 업로드
            String imageKey = null;
            if (article.imageUrl() != null && imageStorageClient.isEnabled()) {
                imageKey = downloadAndUploadImage(provider.id(), article.imageUrl());
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
            entity.setImageKey(imageKey);
            repository.save(entity);
            savedCount++;
            log.debug("[{}] 저장 완료: {}", provider.id(), url);
        }

        log.info("[{}] 크롤링 종료: 수집={}건, 저장={}건, 스킵={}건", provider.id(), articleUrls.size(), savedCount, skippedCount);
    }

    /**
     * 이미지를 다운로드하여 MinIO에 업로드한다.
     * 실패 시 null 반환 (기사 저장에는 영향 없음).
     */
    private String downloadAndUploadImage(String source, String imageUrl) {
        try {
            ResponseEntity<byte[]> response = restClient.get()
                    .uri(imageUrl)
                    .retrieve()
                    .toEntity(byte[].class);

            byte[] data = response.getBody();
            if (data == null || data.length == 0) {
                return null;
            }

            // Content-Type 추출, 기본값 image/jpeg
            String contentType = "image/jpeg";
            MediaType mediaType = response.getHeaders().getContentType();
            if (mediaType != null) {
                contentType = mediaType.toString();
            }

            String key = imageStorageClient.generateKey(source, imageUrl);
            return imageStorageClient.upload(data, contentType, key);
        } catch (Exception e) {
            log.debug("이미지 다운로드/업로드 실패: url={}, error={}", imageUrl, e.getMessage());
            return null;
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
            log.warn("sitemap 파싱 실패: url={}, error={}", sitemapUrl, e.getMessage());
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

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
