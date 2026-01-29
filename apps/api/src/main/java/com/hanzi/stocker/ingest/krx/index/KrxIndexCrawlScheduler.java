package com.hanzi.stocker.ingest.krx.index;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class KrxIndexCrawlScheduler {

    private final KrxIndexCrawlEngine engine;

    public KrxIndexCrawlScheduler(KrxIndexCrawlEngine engine) {
        this.engine = engine;
    }

    @Scheduled(cron = "0 0 18 * * MON-FRI")
    public void run() {
        engine.crawl(LocalDate.now());
    }
}
