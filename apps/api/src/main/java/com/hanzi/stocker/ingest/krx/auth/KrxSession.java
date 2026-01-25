package com.hanzi.stocker.ingest.krx.auth;

import java.time.Instant;
import java.util.Objects;

/**
 * KRX 인증 세션을 나타내는 값 객체.
 * JSESSIONID와 회원번호를 캡슐화.
 */
public final class KrxSession {

    private final String sessionId;
    private final String memberNo;
    private final Instant createdAt;
    private Instant lastValidatedAt;

    public KrxSession(String sessionId, String memberNo) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId must not be null");
        this.memberNo = memberNo;
        this.createdAt = Instant.now();
        this.lastValidatedAt = this.createdAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getMemberNo() {
        return memberNo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastValidatedAt() {
        return lastValidatedAt;
    }

    public void markValidated() {
        this.lastValidatedAt = Instant.now();
    }

    public String toCookieValue() {
        return "JSESSIONID=" + sessionId;
    }

    @Override
    public String toString() {
        return "KrxSession{memberNo='" + memberNo + "', createdAt=" + createdAt + "}";
    }
}
