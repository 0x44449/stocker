package com.hanzi.stocker.ingest.krx.common;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

// session 생성된 시간 기록
public class KrxSession {

    public static final String SESSION_PREFIX = "JSESSIONID=";

    private String sessionId;
    private LocalDate createdAt;

    public KrxSession(
            String sessionId
    ) {
        this.sessionId = sessionId;
        this.createdAt = LocalDate.now();
    }

    public boolean isExpired() {
        // 20분 지나면 만료
        long minutesElapsed = ChronoUnit.MINUTES.between(createdAt, LocalDate.now());
        return minutesElapsed > 20;
    }

    public String toCookieValue() {
        return SESSION_PREFIX + sessionId;
    }
}
