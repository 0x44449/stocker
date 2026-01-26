package com.hanzi.stocker.ingest.krx.investor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 투자자별 거래실적 일별 데이터 적재 서비스.
 */
@Service
public class InvestorFlowDailyRawService {

    private static final Logger crawlLog = LoggerFactory.getLogger("CRAWL");

    private final InvestorFlowDailyRawRepository repository;

    public InvestorFlowDailyRawService(InvestorFlowDailyRawRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public int saveAll(List<InvestorFlowDailyRaw> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }

        crawlLog.info("event=KRX_INVESTOR_SAVE_START count={}", rows.size());

        Instant now = Instant.now();
        int savedCount = 0;

        for (InvestorFlowDailyRaw row : rows) {
            try {
                repository.upsert(
                        row.getTrdDd(),
                        row.getMarket(),
                        row.getInvestorName(),
                        row.getSellVolume(),
                        row.getBuyVolume(),
                        row.getNetVolume(),
                        row.getSellValue(),
                        row.getBuyValue(),
                        row.getNetValue(),
                        row.getSource(),
                        now
                );
                savedCount++;
            } catch (Exception e) {
                crawlLog.warn("event=KRX_INVESTOR_SAVE_ROW_FAILED investorName={} reason={}",
                        row.getInvestorName(), e.getMessage());
            }
        }

        crawlLog.info("event=KRX_INVESTOR_SAVE_SUCCESS saved={} total={}", savedCount, rows.size());

        return savedCount;
    }
}
