package com.hanzi.stocker.ingest.krx.index;

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

/**
 * KRX 지수 크롤링 Job 서비스.
 */
@Service
public class KrxIndexCrawlJobService {

    private static final Logger crawlLog = LoggerFactory.getLogger("CRAWL");
    private static final DateTimeFormatter JOB_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private final KrxSessionProvider sessionProvider;
    private final KrxIndexFetcher indexFetcher;
    private final KrxCrawlConfig config;
    private final KrxIndexCrawlLock crawlLock;

    public KrxIndexCrawlJobService(
            KrxSessionProvider sessionProvider,
            KrxIndexFetcher indexFetcher,
            KrxCrawlConfig config,
            KrxIndexCrawlLock crawlLock) {
        this.sessionProvider = sessionProvider;
        this.indexFetcher = indexFetcher;
        this.config = config;
        this.crawlLock = crawlLock;
    }

    /**
     * 지수 데이터 수집 실행.
     */
    public CrawlResult run(LocalDate trdDd) {
        if (!crawlLock.tryAcquire()) {
            crawlLog.warn("event=KRX_INDEX_JOB_SKIPPED reason=ALREADY_RUNNING");
            return CrawlResult.skipped("Crawl already in progress");
        }

        String jobId = generateJobId();
        long startMs = System.currentTimeMillis();

        crawlLog.info("event=KRX_INDEX_JOB_STARTED jobId={} trdDd={}", jobId, trdDd);

        try {
            if (!config.hasCredentials()) {
                crawlLog.error("event=KRX_INDEX_JOB_ERROR jobId={} reason=NO_CREDENTIALS", jobId);
                return CrawlResult.failed(jobId, "KRX credentials not configured");
            }

            KrxSession session = login(jobId);
            if (session == null) {
                return CrawlResult.failed(jobId, "Login failed");
            }

            KrxIndexFetcher.FetchResult fetchResult = indexFetcher.fetch(session, trdDd);
            long durationMs = System.currentTimeMillis() - startMs;

            if (fetchResult.isSuccess()) {
                crawlLog.info("event=KRX_INDEX_JOB_FINISHED jobId={} trdDd={} durationMs={} parsed={} saved={}",
                        jobId, trdDd, durationMs, fetchResult.parsedCount(), fetchResult.savedCount());
                return CrawlResult.success(jobId, fetchResult.parsedCount(), fetchResult.savedCount(), durationMs);
            } else {
                crawlLog.warn("event=KRX_INDEX_JOB_FAILED jobId={} trdDd={} durationMs={} reason={}",
                        jobId, trdDd, durationMs, fetchResult.error());
                return CrawlResult.failed(jobId, fetchResult.error());
            }

        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startMs;
            crawlLog.error("event=KRX_INDEX_JOB_ERROR jobId={} durationMs={} reason={}",
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
            crawlLog.error("event=KRX_INDEX_JOB_LOGIN_FAILED jobId={} reason={}", jobId, e.getErrorType());
            return null;
        }
    }

    private String generateJobId() {
        return "IDX_" + LocalDateTime.now().format(JOB_ID_FORMAT);
    }

    public record CrawlResult(
            boolean success,
            String jobId,
            String error,
            int parsedCount,
            int savedCount,
            long durationMs
    ) {
        public static CrawlResult success(String jobId, int parsedCount, int savedCount, long durationMs) {
            return new CrawlResult(true, jobId, null, parsedCount, savedCount, durationMs);
        }

        public static CrawlResult failed(String jobId, String error) {
            return new CrawlResult(false, jobId, error, 0, 0, 0);
        }

        public static CrawlResult skipped(String reason) {
            return new CrawlResult(false, null, reason, 0, 0, 0);
        }
    }
}
