package com.hanzi.stocker.ingest.krx.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class KrxAuthClient {

    private static final Logger log = LoggerFactory.getLogger(KrxAuthClient.class);

    private static final String LOGIN_URL = "https://data.krx.co.kr/contents/MDC/COMS/client/MDCCOMS001D1.cmd";

    private final RestClient restClient;

    public KrxAuthClient() {
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();
    }

    public KrxSession login(String userId, String password) {
        log.info("KRX 로그인 시도 - userId: {}", userId);

        var formData = new LinkedMultiValueMap<String, String>();
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
                .toEntity(String.class);

        log.info("로그인 응답 body: {}", response.getBody());

        var sessionId = extractSessionId(response.getHeaders().get(HttpHeaders.SET_COOKIE));

        if (sessionId != null) {
            log.info("KRX 로그인 성공 - sessionId: {}", sessionId);
        } else {
            log.error("KRX 로그인 실패 - 세션 ID 추출 실패, cookies: {}", response.getHeaders().get(HttpHeaders.SET_COOKIE));
        }

        return sessionId != null ? new KrxSession(sessionId) : null;
    }

    String extractSessionId(List<String> cookies) {
        if (cookies == null) {
            return null;
        }
        for (var cookie : cookies) {
            if (cookie.startsWith(KrxSession.SESSION_PREFIX)) {
                int endIndex = cookie.indexOf(';');
                return (endIndex == -1)
                        ? cookie.substring(KrxSession.SESSION_PREFIX.length())
                        : cookie.substring(KrxSession.SESSION_PREFIX.length(), endIndex);
            }
        }
        return null;
    }
}
