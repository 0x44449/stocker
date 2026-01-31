package com.hanzi.stocker.ingest.krx.common;

import com.hanzi.stocker.ingest.krx.KrxCrawlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KrxSessionProvider {

    private static final Logger log = LoggerFactory.getLogger(KrxSessionProvider.class);

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
            log.debug("기존 세션 재사용");
            return session;
        }

        synchronized (lock) {
            if (session == null || session.isExpired()) {
                log.info("새 세션 획득 시도");
                session = authClient.login(config.getUsername(), config.getPassword());
                if (session == null) {
                    log.error("세션 획득 실패");
                }
            }
            return session;
        }
    }
}
