package com.hanzi.stocker.ingest.krx.investor;

import com.hanzi.stocker.ingest.krx.KrxCrawlConfig;
import com.hanzi.stocker.ingest.krx.auth.KrxLoginException;
import com.hanzi.stocker.ingest.krx.auth.KrxSession;
import com.hanzi.stocker.ingest.krx.auth.KrxSessionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * KRX 투자자별 거래실적 크롤링 Job 서비스.
 */
@Service
public class KrxInvestorFlowCrawlJobService {

    private static final Logger crawlLog = LoggerFactory.getLogger("CRAWL");
    private static final DateTimeFormatter JOB_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private static final List<String> DEFAULT_MARKETS = List.of("STK", "KSQ");

    private final KrxSessionProvider sessionProvider;
    private final KrxInvestorFlowFetcher investorFetcher;
    private final KrxCrawlConfig config;
    private final KrxInvestorFlowCrawlLock crawlLock;

    public KrxInvestorFlowCrawlJobService(
            KrxSessionProvider sessionProvider,
            KrxInvestorFlowFetcher investorFetcher,
            KrxCrawlConfig config,
            KrxInvestorFlowCrawlLock crawlLock) {
        this.sessionProvider = sessionProvider;
        this.investorFetcher = investorFetcher;
        this.config = config;
        this.crawlLock = crawlLock;
    }

    /**
     * 모든 시장(STK, KSQ) 투자자별 데이터 수집.
     */
    public CrawlResult runAll(LocalDate trdDd) {
        return runMarkets(trdDd, DEFAULT_MARKETS);
    }

    /**
     * 특정 시장 투자자별 데이터 수집.
     */
    public CrawlResult run(LocalDate trdDd, String market) {
        return runMarkets(trdDd, List.of(market));
    }

    /**
     * 지정된 시장들의 투자자별 데이터 수집.
     */
    public CrawlResult runMarkets(LocalDate trdDd, List<String> markets) {
        if (!crawlLock.tryAcquire()) {
            crawlLog.warn("event=KRX_INVESTOR_JOB_SKIPPED reason=ALREADY_RUNNING");
            return CrawlResult.skipped("Crawl already in progress");
        }

        String jobId = generateJobId();
        long startMs = System.currentTimeMillis();

        crawlLog.info("event=KRX_INVESTOR_JOB_STARTED jobId={} trdDd={} markets={}", jobId, trdDd, markets);

        try {
            if (!config.hasCredentials()) {
                crawlLog.error("event=KRX_INVESTOR_JOB_ERROR jobId={} reason=NO_CREDENTIALS", jobId);
                return CrawlResult.failed(jobId, "KRX credentials not configured");
            }

            KrxSession session = login(jobId);
            if (session == null) {
                return CrawlResult.failed(jobId, "Login failed");
            }

            List<MarketResult> marketResults = new ArrayList<>();
            int totalParsed = 0;
            int totalSaved = 0;

            for (String market : markets) {
                KrxInvestorFlowFetcher.FetchResult fetchResult = investorFetcher.fetch(session, trdDd, market);
                marketResults.add(new MarketResult(market, fetchResult));
                totalParsed += fetchResult.parsedCount();
                totalSaved += fetchResult.savedCount();
            }

            long durationMs = System.currentTimeMillis() - startMs;

            crawlLog.info("event=KRX_INVESTOR_JOB_FINISHED jobId={} trdDd={} durationMs={} markets={} totalParsed={} totalSaved={}",
                    jobId, trdDd, durationMs, markets.size(), totalParsed, totalSaved);

            return CrawlResult.success(jobId, marketResults, totalParsed, totalSaved, durationMs);

        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startMs;
            crawlLog.error("event=KRX_INVESTOR_JOB_ERROR jobId={} durationMs={} reason={}",
                    jobId, durationMs, e.getMessage(), e);
            return CrawlResult.failed(jobId, e.getMessage());
        } finally {
            crawlLock.release();
        }
    }

    public boolean isRunning() {
        return crawlLock.isRunning();
    }

    private KrxSession login(String jobId) {
        try {
            return sessionProvider.login(config.getUsername(), config.getPassword());
        } catch (KrxLoginException e) {
            crawlLog.error("event=KRX_INVESTOR_JOB_LOGIN_FAILED jobId={} reason={}", jobId, e.getErrorType());
            return null;
        }
    }

    private String generateJobId() {
        return "INV_" + LocalDateTime.now().format(JOB_ID_FORMAT);
    }

    public record CrawlResult(
            boolean success,
            String jobId,
            String error,
            List<MarketResult> marketResults,
            int totalParsedCount,
            int totalSavedCount,
            long durationMs
    ) {
        public static CrawlResult success(String jobId, List<MarketResult> marketResults,
                                          int totalParsed, int totalSaved, long durationMs) {
            return new CrawlResult(true, jobId, null, marketResults, totalParsed, totalSaved, durationMs);
        }

        public static CrawlResult failed(String jobId, String error) {
            return new CrawlResult(false, jobId, error, List.of(), 0, 0, 0);
        }

        public static CrawlResult skipped(String reason) {
            return new CrawlResult(false, null, reason, List.of(), 0, 0, 0);
        }
    }

    public record MarketResult(String market, KrxInvestorFlowFetcher.FetchResult fetchResult) {}
}
