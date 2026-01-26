package com.hanzi.stocker.ingest.krx.investor;

import com.hanzi.stocker.ingest.krx.auth.KrxSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * KRX 투자자별 거래실적 수집 Fetcher.
 * OTP 생성 → CSV 다운로드 → 파싱 → 적재 흐름을 조율.
 */
@Component
public class KrxInvestorFlowFetcher {

    private static final Logger crawlLog = LoggerFactory.getLogger("CRAWL");

    private final KrxInvestorFlowOtpClient otpClient;
    private final KrxInvestorFlowCsvDownloader csvDownloader;
    private final KrxInvestorFlowCsvParser csvParser;
    private final InvestorFlowDailyRawService rawService;

    public KrxInvestorFlowFetcher(
            KrxInvestorFlowOtpClient otpClient,
            KrxInvestorFlowCsvDownloader csvDownloader,
            KrxInvestorFlowCsvParser csvParser,
            InvestorFlowDailyRawService rawService) {
        this.otpClient = otpClient;
        this.csvDownloader = csvDownloader;
        this.csvParser = csvParser;
        this.rawService = rawService;
    }

    public FetchResult fetch(KrxSession session, LocalDate trdDd, String market) {
        crawlLog.info("event=KRX_INVESTOR_FETCH_START trdDd={} market={}", trdDd, market);
        long startTime = System.currentTimeMillis();

        try {
            // 1. OTP 생성
            String otp = otpClient.generateOtp(session, trdDd, market);

            // 2. CSV 다운로드
            byte[] csvBytes = csvDownloader.download(session, otp);

            // 3. CSV 파싱
            List<InvestorFlowDailyRaw> rows = csvParser.parse(csvBytes, trdDd, market);

            // 4. DB 적재
            int savedCount = rawService.saveAll(rows);

            long durationMs = System.currentTimeMillis() - startTime;
            crawlLog.info("event=KRX_INVESTOR_FETCH_SUCCESS trdDd={} market={} parsed={} saved={} durationMs={}",
                    trdDd, market, rows.size(), savedCount, durationMs);

            return new FetchResult(rows.size(), savedCount, null);

        } catch (KrxInvestorFlowException e) {
            long durationMs = System.currentTimeMillis() - startTime;
            crawlLog.warn("event=KRX_INVESTOR_FETCH_FAILED trdDd={} market={} reason={} durationMs={}",
                    trdDd, market, e.getErrorType(), durationMs);
            return new FetchResult(0, 0, e.getMessage());

        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            crawlLog.warn("event=KRX_INVESTOR_FETCH_FAILED trdDd={} market={} reason=UNKNOWN durationMs={}",
                    trdDd, market, durationMs);
            return new FetchResult(0, 0, e.getMessage());
        }
    }

    public record FetchResult(int parsedCount, int savedCount, String error) {
        public boolean isSuccess() {
            return error == null;
        }
    }
}
