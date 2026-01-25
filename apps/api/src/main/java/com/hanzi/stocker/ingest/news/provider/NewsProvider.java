package com.hanzi.stocker.ingest.news.provider;

import com.hanzi.stocker.ingest.news.model.ParsedArticle;

import java.util.List;

public interface NewsProvider {

    String id();

    String baseUrl();

    String press();

    List<String> sitemapHints();

    boolean isArticleUrl(String url);

    ParsedArticle parseArticle(String html, String url);
}
