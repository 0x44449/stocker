package app.sandori.stocker.api.ingest.krx.investor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class KrxInvestorCrawlScheduler {

    private final KrxInvestorCrawlEngine engine;

    public KrxInvestorCrawlScheduler(KrxInvestorCrawlEngine engine) {
        this.engine = engine;
    }

    @Scheduled(cron = "0 0 18 * * MON-FRI")
    public void run() {
        engine.crawl(LocalDate.now(), "STK");
    }
}
