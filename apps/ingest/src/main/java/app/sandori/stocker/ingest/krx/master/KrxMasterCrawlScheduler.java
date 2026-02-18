package app.sandori.stocker.ingest.krx.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KrxMasterCrawlScheduler {

    private static final Logger log = LoggerFactory.getLogger(KrxMasterCrawlScheduler.class);

    private final KrxMasterCrawlEngine engine;

    public KrxMasterCrawlScheduler(KrxMasterCrawlEngine engine) {
        this.engine = engine;
    }

    // 매일 09:10
    @Scheduled(cron = "0 10 9 * * *")
    public void run() {
        log.info("종목 마스터 크롤링 스케줄 시작");
        engine.crawl();
    }
}
