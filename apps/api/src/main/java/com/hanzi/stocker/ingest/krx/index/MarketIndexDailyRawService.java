package com.hanzi.stocker.ingest.krx.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 시장 지수 일별 데이터 적재 서비스.
 */
@Service
public class MarketIndexDailyRawService {

    private static final Logger crawlLog = LoggerFactory.getLogger("CRAWL");

    private final MarketIndexDailyRawRepository repository;

    public MarketIndexDailyRawService(MarketIndexDailyRawRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public int saveAll(List<MarketIndexDailyRaw> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }

        crawlLog.info("event=KRX_INDEX_SAVE_START count={}", rows.size());

        Instant now = Instant.now();
        int savedCount = 0;

        for (MarketIndexDailyRaw row : rows) {
            try {
                repository.upsert(
                        row.getTrdDd(),
                        row.getIndexName(),
                        row.getClose(),
                        row.getDiff(),
                        row.getDiffRate(),
                        row.getOpen(),
                        row.getHigh(),
                        row.getLow(),
                        row.getVolume(),
                        row.getValue(),
                        row.getMarketCap(),
                        row.getSource(),
                        now
                );
                savedCount++;
            } catch (Exception e) {
                crawlLog.warn("event=KRX_INDEX_SAVE_ROW_FAILED indexName={} reason={}",
                        row.getIndexName(), e.getMessage());
            }
        }

        crawlLog.info("event=KRX_INDEX_SAVE_SUCCESS saved={} total={}", savedCount, rows.size());

        return savedCount;
    }
}
