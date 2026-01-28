package com.hanzi.stocker.ingest.krx.common;

public class KrxSessionProvider {

    private static volatile KrxSession session;
    private static final Object lock = new Object();

    private KrxSessionProvider() {}

    public static KrxSession get() {
        if (session != null && !session.isExpired()) {
            return session;
        }

        synchronized (lock) {
            if (session == null || session.isExpired()) {
                var authClient = new KrxAuthClient();
                session = authClient.login("", "");
            }
            return session;
        }
    }
}
