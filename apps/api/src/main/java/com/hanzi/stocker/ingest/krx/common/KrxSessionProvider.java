package com.hanzi.stocker.ingest.krx.common;

import com.hanzi.stocker.ingest.krx.KrxCrawlConfig;
import org.springframework.stereotype.Component;

@Component
public class KrxSessionProvider {

    private final KrxCrawlConfig config;
    private final KrxAuthClient authClient;

    private volatile KrxSession session;
    private final Object lock = new Object();

    public KrxSessionProvider(KrxCrawlConfig config, KrxAuthClient authClient) {
        this.config = config;
        this.authClient = authClient;
    }

    public KrxSession get() {
        if (session != null && !session.isExpired()) {
            return session;
        }

        synchronized (lock) {
            if (session == null || session.isExpired()) {
                session = authClient.login(config.getUsername(), config.getPassword());
            }
            return session;
        }
    }
}
