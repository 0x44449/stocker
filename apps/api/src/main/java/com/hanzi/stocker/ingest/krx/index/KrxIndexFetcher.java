package com.hanzi.stocker.ingest.krx.index;

import com.hanzi.stocker.ingest.krx.auth.KrxSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * KRX 전체 지수 시세 수집 Fetcher.
 * OTP 생성 → CSV 다운로드 → 파싱 → 적재 흐름을 조율.
 */
@Component
public class KrxIndexFetcher {

    private static final Logger crawlLog = LoggerFactory.getLogger("CRAWL");

    private final KrxIndexOtpClient otpClient;
    private final KrxIndexCsvDownloader csvDownloader;
    private final KrxIndexCsvParser csvParser;
    private final MarketIndexDailyRawService rawService;

    public KrxIndexFetcher(
            KrxIndexOtpClient otpClient,
            KrxIndexCsvDownloader csvDownloader,
            KrxIndexCsvParser csvParser,
            MarketIndexDailyRawService rawService) {
        this.otpClient = otpClient;
        this.csvDownloader = csvDownloader;
        this.csvParser = csvParser;
        this.rawService = rawService;
    }

    public FetchResult fetch(KrxSession session, LocalDate trdDd) {
        crawlLog.info("event=KRX_INDEX_FETCH_START trdDd={}", trdDd);
        long startTime = System.currentTimeMillis();

        try {
            // 1. OTP 생성
            String otp = otpClient.generateOtp(session, trdDd);

            // 2. CSV 다운로드
            byte[] csvBytes = csvDownloader.download(session, otp);

            // 3. CSV 파싱
            List<MarketIndexDailyRaw> rows = csvParser.parse(csvBytes, trdDd);

            // 4. DB 적재
            int savedCount = rawService.saveAll(rows);

            long durationMs = System.currentTimeMillis() - startTime;
            crawlLog.info("event=KRX_INDEX_FETCH_SUCCESS trdDd={} parsed={} saved={} durationMs={}",
                    trdDd, rows.size(), savedCount, durationMs);

            return new FetchResult(rows.size(), savedCount, null);

        } catch (KrxIndexException e) {
            long durationMs = System.currentTimeMillis() - startTime;
            crawlLog.warn("event=KRX_INDEX_FETCH_FAILED trdDd={} reason={} durationMs={}",
                    trdDd, e.getErrorType(), durationMs);
            return new FetchResult(0, 0, e.getMessage());

        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            crawlLog.warn("event=KRX_INDEX_FETCH_FAILED trdDd={} reason=UNKNOWN durationMs={}",
                    trdDd, durationMs);
            return new FetchResult(0, 0, e.getMessage());
        }
    }

    public record FetchResult(int parsedCount, int savedCount, String error) {
        public boolean isSuccess() {
            return error == null;
        }
    }
}
