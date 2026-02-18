package app.sandori.stocker.ingest.krx.stock;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class KrxStockCrawlScheduler {

    private final KrxStockCrawlEngine engine;

    public KrxStockCrawlScheduler(KrxStockCrawlEngine engine) {
        this.engine = engine;
    }

    @Scheduled(cron = "0 0 18 * * MON-FRI")
    public void run() {
        engine.crawl(LocalDate.now(), "KOSPI");
    }
}
