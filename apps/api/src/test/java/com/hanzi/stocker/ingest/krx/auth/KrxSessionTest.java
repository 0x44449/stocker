package com.hanzi.stocker.ingest.krx.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KrxSessionTest {

    @Test
    void constructor_withValidSessionId_createsSession() {
        KrxSession session = new KrxSession("ABC123", "1000005089");

        assertEquals("ABC123", session.getSessionId());
        assertEquals("1000005089", session.getMemberNo());
        assertNotNull(session.getCreatedAt());
        assertNotNull(session.getLastValidatedAt());
    }

    @Test
    void constructor_withNullSessionId_throwsException() {
        assertThrows(NullPointerException.class, () -> new KrxSession(null, "1000005089"));
    }

    @Test
    void toCookieValue_returnsCorrectFormat() {
        KrxSession session = new KrxSession("ABC123", "1000005089");

        assertEquals("JSESSIONID=ABC123", session.toCookieValue());
    }

    @Test
    void markValidated_updatesLastValidatedAt() throws InterruptedException {
        KrxSession session = new KrxSession("ABC123", "1000005089");
        var initialValidatedAt = session.getLastValidatedAt();

        Thread.sleep(10);
        session.markValidated();

        assertTrue(session.getLastValidatedAt().isAfter(initialValidatedAt));
    }
}
