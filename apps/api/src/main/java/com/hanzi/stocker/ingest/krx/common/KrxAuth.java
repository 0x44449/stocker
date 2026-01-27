package com.hanzi.stocker.ingest.krx.common;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class KrxAuth {

    private static final String LOGIN_URL = "https://data.krx.co.kr/contents/MDC/COMS/client/MDCCOMS001D1.cmd";

    private final RestClient restClient;

    public KrxAuth() {
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();
    }

    public LoginSession login(String userId, String password) {
        var formData = new LinkedMultiValueMap<>();
        formData.add("mbrNm", "");
        formData.add("telNo", "");
        formData.add("di", "");
        formData.add("certType", "");
        formData.add("mbrId", userId);
        formData.add("pw", password);
        formData.add("skipDup", "Y");

        var response = restClient.post()
                .uri(LOGIN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .toBodilessEntity();

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies == null) {
            throw new IllegalStateException("No Set-Cookie header in response");
        }

        for (String cookie : cookies) {
            if (cookie.startsWith("JSESSIONID=")) {
                int endIndex = cookie.indexOf(';');
                String sessionId = (endIndex == -1)
                        ? cookie.substring("JSESSIONID=".length())
                        : cookie.substring("JSESSIONID=".length(), endIndex);
                return new LoginSession(sessionId);
            }
        }

        throw new IllegalStateException("JSESSIONID not found in Set-Cookie headers");
    }

    public record LoginSession(String sessionId) {
        public String toCookieValue() {
            return "JSESSIONID=" + sessionId;
        }
    }
}
