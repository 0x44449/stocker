package app.sandori.stocker.ingest.krx.stock;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class KrxStockRealtimeCrawlScheduler {

    private final KrxStockRealtimeCrawlEngine engine;

    public KrxStockRealtimeCrawlScheduler(KrxStockRealtimeCrawlEngine engine) {
        this.engine = engine;
    }

    // 평일 09:00~15:50, 10분 간격
    @Scheduled(cron = "0 */10 9-15 * * MON-FRI")
    public void run() {
        engine.crawl(LocalDate.now(), "KOSPI");
    }
}
