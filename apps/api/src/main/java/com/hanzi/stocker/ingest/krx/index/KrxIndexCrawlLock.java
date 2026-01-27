package com.hanzi.stocker.ingest.krx.index;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * KRX 지수 크롤링 중복 실행 방지용 락.
 */
@Component
public class KrxIndexCrawlLock {

    private final AtomicBoolean running = new AtomicBoolean(false);

    public boolean tryAcquire() {
        return running.compareAndSet(false, true);
    }

    public void release() {
        running.set(false);
    }

    public boolean isRunning() {
        return running.get();
    }
}
