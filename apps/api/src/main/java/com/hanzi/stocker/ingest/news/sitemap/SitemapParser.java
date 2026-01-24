package com.hanzi.stocker.ingest.news.sitemap;

import org.springframework.stereotype.Component;
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

@Component
public class SitemapParser {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ISO_DATE;

    public List<SitemapEntry> parse(String xml) {
        List<SitemapEntry> entries = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

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
            throw new RuntimeException("Failed to parse sitemap XML", e);
        }

        return entries;
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
            return LocalDateTime.parse(lastmod, ISO_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return java.time.LocalDate.parse(lastmod, DATE_ONLY_FORMATTER).atStartOfDay();
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
}
