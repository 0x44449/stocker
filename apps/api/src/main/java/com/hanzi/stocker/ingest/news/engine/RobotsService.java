package com.hanzi.stocker.ingest.news.engine;

import com.hanzi.stocker.ingest.news.model.FetchResult;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RobotsService {

    private final HttpFetcher httpFetcher;

    public RobotsService(HttpFetcher httpFetcher) {
        this.httpFetcher = httpFetcher;
    }

    public RobotsPolicy fetch(String baseUrl, String userAgent) {
        String robotsUrl = baseUrl + "/robots.txt";
        FetchResult result = httpFetcher.fetch(robotsUrl, userAgent);

        if (!result.isSuccess()) {
            return new RobotsPolicy(Set.of(), List.of());
        }

        return parse(result.body());
    }

    private RobotsPolicy parse(String robotsTxt) {
        Set<String> disallowedPaths = new HashSet<>();
        List<String> sitemapUrls = new ArrayList<>();
        boolean groupHasWildcard = false;
        boolean inDirectiveSection = false;

        try (BufferedReader reader = new BufferedReader(new StringReader(robotsTxt))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String lowerLine = line.toLowerCase();

                if (lowerLine.startsWith("user-agent:")) {
                    // New user-agent after directives = new group
                    if (inDirectiveSection) {
                        groupHasWildcard = false;
                        inDirectiveSection = false;
                    }
                    String agent = line.substring("user-agent:".length()).trim();
                    if (agent.equals("*")) {
                        groupHasWildcard = true;
                    }
                } else if (lowerLine.startsWith("disallow:")) {
                    inDirectiveSection = true;
                    if (groupHasWildcard) {
                        String path = line.substring("disallow:".length()).trim();
                        if (!path.isEmpty()) {
                            disallowedPaths.add(path);
                        }
                    }
                } else if (lowerLine.startsWith("allow:")) {
                    inDirectiveSection = true;
                } else if (lowerLine.startsWith("sitemap:")) {
                    String sitemapUrl = line.substring("sitemap:".length()).trim();
                    if (!sitemapUrl.isEmpty()) {
                        sitemapUrls.add(sitemapUrl);
                    }
                }
            }
        } catch (Exception e) {
            // ignore parse errors
        }

        return new RobotsPolicy(disallowedPaths, sitemapUrls);
    }

    public record RobotsPolicy(
            Set<String> disallowedPaths,
            List<String> sitemapUrls
    ) {
        public boolean isAllowed(String path) {
            for (String disallowed : disallowedPaths) {
                if (path.startsWith(disallowed)) {
                    return false;
                }
            }
            return true;
        }
    }
}
