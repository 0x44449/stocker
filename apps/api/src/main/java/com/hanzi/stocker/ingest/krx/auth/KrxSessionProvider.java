package com.hanzi.stocker.ingest.krx.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * KRX 로그인을 수행하고 세션을 생성하는 단일 진입점.
 */
@Component
public class KrxSessionProvider {

    private static final Logger crawlLog = LoggerFactory.getLogger("CRAWL");

    private final KrxAuthClient authClient;
    private final ObjectMapper objectMapper;

    public KrxSessionProvider(KrxAuthClient authClient) {
        this.authClient = authClient;
        this.objectMapper = new ObjectMapper();
    }

    public KrxSession login(String mbrId, String pw) {
        crawlLog.info("event=KRX_LOGIN_START mbrId={}", mbrId);

        var formData = new KrxLoginRequestBuilder()
                .mbrId(mbrId)
                .pw(pw)
                .build();

        KrxAuthClient.LoginResult result;
        try {
            result = authClient.login(formData);
        } catch (Exception e) {
            crawlLog.warn("event=KRX_LOGIN_FAILED mbrId={} reason=HTTP_ERROR", mbrId);
            throw new KrxLoginException(
                    KrxLoginException.ErrorType.UNKNOWN_RESPONSE,
                    "HTTP request failed: " + e.getMessage(),
                    e
            );
        }

        KrxLoginResponse response = parseResponse(mbrId, result.body());
        validateResponse(mbrId, response);

        String sessionId = result.sessionId();
        if (sessionId == null || sessionId.isBlank()) {
            crawlLog.warn("event=KRX_LOGIN_FAILED mbrId={} reason=SESSION_NOT_ISSUED", mbrId);
            throw new KrxLoginException(
                    KrxLoginException.ErrorType.SESSION_NOT_ISSUED,
                    "Login succeeded but JSESSIONID was not issued"
            );
        }

        KrxSession session = new KrxSession(sessionId, response.getMbrNo());
        crawlLog.info("event=KRX_LOGIN_SUCCESS mbrId={} memberNo={}", mbrId, response.getMbrNo());

        return session;
    }

    private KrxLoginResponse parseResponse(String mbrId, String body) {
        if (body == null || body.isBlank()) {
            crawlLog.warn("event=KRX_LOGIN_FAILED mbrId={} reason=EMPTY_RESPONSE", mbrId);
            throw new KrxLoginException(
                    KrxLoginException.ErrorType.UNKNOWN_RESPONSE,
                    "Empty response body from KRX"
            );
        }

        try {
            return objectMapper.readValue(body, KrxLoginResponse.class);
        } catch (JsonProcessingException e) {
            crawlLog.warn("event=KRX_LOGIN_FAILED mbrId={} reason=PARSE_FAILED", mbrId);
            throw new KrxLoginException(
                    KrxLoginException.ErrorType.UNKNOWN_RESPONSE,
                    "Failed to parse KRX login response",
                    e
            );
        }
    }

    private void validateResponse(String mbrId, KrxLoginResponse response) {
        if (!response.isSuccess()) {
            crawlLog.warn("event=KRX_LOGIN_FAILED mbrId={} reason=INVALID_CREDENTIALS errorCode={}",
                    mbrId, response.getErrorCode());
            throw new KrxLoginException(
                    KrxLoginException.ErrorType.INVALID_CREDENTIALS,
                    "KRX login failed: " + response.getErrorCode() + " - " + response.getErrorMessage()
            );
        }
    }
}
