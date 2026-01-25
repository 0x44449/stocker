package com.hanzi.stocker.ingest.news.engine;

import com.hanzi.stocker.ingest.news.model.FetchResult;
import com.hanzi.stocker.ingest.news.model.SitemapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SitemapService {

    private static final Logger log = LoggerFactory.getLogger(SitemapService.class);
    private static final int MAX_INDEX_DEPTH = 2;

    private final HttpFetcher httpFetcher;

    public SitemapService(HttpFetcher httpFetcher) {
        this.httpFetcher = httpFetcher;
    }

    public Optional<List<SitemapEntry>> fetch(String sitemapUrl, String userAgent) {
        return fetch(sitemapUrl, userAgent, 0);
    }

    private Optional<List<SitemapEntry>> fetch(String sitemapUrl, String userAgent, int depth) {
        if (depth > MAX_INDEX_DEPTH) {
            log.warn("Max sitemap index depth exceeded: {}", sitemapUrl);
            return Optional.empty();
        }

        FetchResult result = httpFetcher.fetch(sitemapUrl, userAgent);

        if (!result.isSuccess()) {
            return Optional.empty();
        }

        return Optional.of(parse(result.body(), userAgent, depth));
    }

    private List<SitemapEntry> parse(String xml, String userAgent, int depth) {
        List<SitemapEntry> entries = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            // Check if this is a sitemap index
            NodeList sitemapNodes = doc.getElementsByTagName("sitemap");
            if (sitemapNodes.getLength() > 0) {
                log.debug("Detected sitemap index with {} child sitemaps", sitemapNodes.getLength());
                return parseIndex(sitemapNodes, userAgent, depth);
            }

            // Regular sitemap with <url> elements
            NodeList urlNodes = doc.getElementsByTagName("url");

            for (int i = 0; i < urlNodes.getLength(); i++) {
                Element urlElement = (Element) urlNodes.item(i);
                String loc = getElementText(urlElement, "loc");
                String lastmod = getElementText(urlElement, "lastmod");

                if (loc != null && !loc.isBlank()) {
                    LocalDateTime lastModified = parseLastModified(lastmod);
                    entries.add(new SitemapEntry(loc, lastModified));
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse sitemap: {}", e.getMessage());
        }

        return entries;
    }

    private List<SitemapEntry> parseIndex(NodeList sitemapNodes, String userAgent, int depth) {
        List<SitemapEntry> allEntries = new ArrayList<>();

        for (int i = 0; i < sitemapNodes.getLength(); i++) {
            Element sitemapElement = (Element) sitemapNodes.item(i);
            String childUrl = getElementText(sitemapElement, "loc");

            if (childUrl != null && !childUrl.isBlank()) {
                log.debug("Fetching child sitemap: {}", childUrl);
                Optional<List<SitemapEntry>> childEntries = fetch(childUrl, userAgent, depth + 1);
                childEntries.ifPresent(allEntries::addAll);
            }
        }

        return allEntries;
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    private LocalDateTime parseLastModified(String lastmod) {
        if (lastmod == null || lastmod.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(lastmod, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            try {
                return java.time.LocalDate.parse(lastmod, DateTimeFormatter.ISO_DATE).atStartOfDay();
            } catch (DateTimeParseException e2) {
                try {
                    return java.time.OffsetDateTime.parse(lastmod).toLocalDateTime();
                } catch (DateTimeParseException e3) {
                    return null;
                }
            }
        }
    }
}
