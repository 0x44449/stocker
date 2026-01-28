package com.hanzi.stocker.ingest.krx.common;

public record KrxSession(String sessionId) {

    public static final String SESSION_PREFIX = "JSESSIONID=";

    public String toCookieValue() {
        return SESSION_PREFIX + sessionId;
    }
}
