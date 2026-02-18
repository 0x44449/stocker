package app.sandori.stocker.ingest.krx.common;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class KrxSession {

    public static final String SESSION_PREFIX = "JSESSIONID=";

    private String sessionId;
    private LocalDateTime createdAt;

    public KrxSession(String sessionId) {
        this.sessionId = sessionId;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        long minutesElapsed = ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now());
        return minutesElapsed > 20;
    }

    public String toCookieValue() {
        return SESSION_PREFIX + sessionId;
    }
}
