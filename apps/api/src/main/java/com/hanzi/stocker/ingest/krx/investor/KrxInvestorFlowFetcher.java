package com.hanzi.stocker.ingest.krx.investor;

import com.hanzi.stocker.ingest.krx.auth.KrxSession;
import com.hanzi.stocker.ingest.krx.common.KrxDownloadException;
import com.hanzi.stocker.ingest.krx.common.KrxFileDownloadClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.List;

/**
 * KRX 투자자별 거래실적 수집 Fetcher.
 */
@Component
public class KrxInvestorFlowFetcher {

    private static final Logger crawlLog = LoggerFactory.getLogger("CRAWL");

    private final KrxFileDownloadClient downloadClient;
    private final KrxInvestorFlowCsvParser csvParser;
    private final InvestorFlowDailyRawService rawService;

    public KrxInvestorFlowFetcher(
            KrxFileDownloadClient downloadClient,
            KrxInvestorFlowCsvParser csvParser,
            InvestorFlowDailyRawService rawService) {
        this.downloadClient = downloadClient;
        this.csvParser = csvParser;
        this.rawService = rawService;
    }

    public FetchResult fetch(KrxSession session, LocalDate trdDd, String market) {
        crawlLog.info("event=KRX_INVESTOR_FETCH_START trdDd={} market={}", trdDd, market);
        long startTime = System.currentTimeMillis();

        try {
            // 1. OTP 생성 + CSV 다운로드
            MultiValueMap<String, String> formData = KrxInvestorFlowRequestSpec.buildOtpFormData(trdDd, market);
            byte[] csvBytes = downloadClient.download(
                    session,
                    KrxInvestorFlowRequestSpec.referer(),
                    formData,
                    KrxInvestorFlowRequestSpec.LOG_PREFIX
            );

            // 2. CSV 파싱
            List<InvestorFlowDailyRaw> rows = csvParser.parse(csvBytes, trdDd, market);

            // 3. DB 적재
            int savedCount = rawService.saveAll(rows);

            long durationMs = System.currentTimeMillis() - startTime;
            crawlLog.info("event=KRX_INVESTOR_FETCH_SUCCESS trdDd={} market={} parsed={} saved={} durationMs={}",
                    trdDd, market, rows.size(), savedCount, durationMs);

            return new FetchResult(rows.size(), savedCount, null);

        } catch (KrxDownloadException e) {
            long durationMs = System.currentTimeMillis() - startTime;
            crawlLog.warn("event=KRX_INVESTOR_FETCH_FAILED trdDd={} market={} reason={} durationMs={}",
                    trdDd, market, e.getErrorType(), durationMs);
            return new FetchResult(0, 0, e.getMessage());

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
