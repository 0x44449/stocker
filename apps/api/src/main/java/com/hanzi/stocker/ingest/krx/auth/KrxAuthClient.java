package com.hanzi.stocker.ingest.krx.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * KRX HTTP 통신을 담당하는 클라이언트.
 * RestClient를 래핑하고 Set-Cookie 헤더에서 JSESSIONID를 추출.
 */
@Component
class KrxAuthClient {

    private static final String LOGIN_URL = "https://data.krx.co.kr/contents/MDC/COMS/client/MDCCOMS001D1.cmd";
    private static final String JSESSIONID_PREFIX = "JSESSIONID=";

    private final RestClient restClient;

    public KrxAuthClient() {
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();
    }

    public LoginResult login(MultiValueMap<String, String> formData) {
        ResponseEntity<String> response = restClient.post()
                .uri(LOGIN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .toEntity(String.class);

        String body = response.getBody();
        String sessionId = extractJSessionId(response.getHeaders());

        return new LoginResult(body, sessionId);
    }

    private String extractJSessionId(HttpHeaders headers) {
        List<String> cookies = headers.get(HttpHeaders.SET_COOKIE);
        if (cookies == null) {
            return null;
        }

        for (String cookie : cookies) {
            if (cookie.startsWith(JSESSIONID_PREFIX)) {
                int endIndex = cookie.indexOf(';');
                if (endIndex == -1) {
                    return cookie.substring(JSESSIONID_PREFIX.length());
                }
                return cookie.substring(JSESSIONID_PREFIX.length(), endIndex);
            }
        }
        return null;
    }

    record LoginResult(String body, String sessionId) {
    }
}
