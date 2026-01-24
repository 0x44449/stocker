package com.hanzi.stocker.ingest.news.crawler;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Component
public class RobotsTxtPolicy {

    private final HttpClient httpClient;

    public RobotsTxtPolicy() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public DisallowRules fetch(String robotsTxtUrl, String userAgent) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(robotsTxtUrl))
                .header("User-Agent", userAgent)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return new DisallowRules(Set.of());
        }

        return parse(response.body());
    }

    private DisallowRules parse(String robotsTxt) throws IOException {
        Set<String> disallowedPaths = new HashSet<>();
        boolean inUserAgentBlock = false;
        boolean isRelevantBlock = false;

        try (BufferedReader reader = new BufferedReader(new StringReader(robotsTxt))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.toLowerCase().startsWith("user-agent:")) {
                    String agent = line.substring("user-agent:".length()).trim();
                    isRelevantBlock = agent.equals("*");
                    inUserAgentBlock = true;
                } else if (inUserAgentBlock && isRelevantBlock && line.toLowerCase().startsWith("disallow:")) {
                    String path = line.substring("disallow:".length()).trim();
                    if (!path.isEmpty()) {
                        disallowedPaths.add(path);
                    }
                }
            }
        }

        return new DisallowRules(disallowedPaths);
    }

    public static class DisallowRules {
        private final Set<String> disallowedPaths;

        public DisallowRules(Set<String> disallowedPaths) {
            this.disallowedPaths = disallowedPaths;
        }

        public boolean isAllowed(String path) {
            for (String disallowed : disallowedPaths) {
                if (path.startsWith(disallowed)) {
                    return false;
                }
            }
            return true;
        }

        public Set<String> getDisallowedPaths() {
            return disallowedPaths;
        }
    }
}
