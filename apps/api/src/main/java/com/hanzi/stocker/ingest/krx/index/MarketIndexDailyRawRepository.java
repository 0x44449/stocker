package com.hanzi.stocker.ingest.krx.index;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public interface MarketIndexDailyRawRepository extends JpaRepository<MarketIndexDailyRaw, MarketIndexDailyRawId> {

    @Modifying
    @Query(value = """
            INSERT INTO market_index_daily_raw
            (trd_dd, index_name, close, diff, diff_rate, open, high, low, volume, value, market_cap, source, ingested_at)
            VALUES (:trdDd, :indexName, :close, :diff, :diffRate, :open, :high, :low, :volume, :value, :marketCap, :source, :ingestedAt)
            ON CONFLICT (trd_dd, index_name) DO UPDATE SET
                close = EXCLUDED.close,
                diff = EXCLUDED.diff,
                diff_rate = EXCLUDED.diff_rate,
                open = EXCLUDED.open,
                high = EXCLUDED.high,
                low = EXCLUDED.low,
                volume = EXCLUDED.volume,
                value = EXCLUDED.value,
                market_cap = EXCLUDED.market_cap,
                ingested_at = EXCLUDED.ingested_at
            """, nativeQuery = true)
    void upsert(
            @Param("trdDd") LocalDate trdDd,
            @Param("indexName") String indexName,
            @Param("close") BigDecimal close,
            @Param("diff") BigDecimal diff,
            @Param("diffRate") BigDecimal diffRate,
            @Param("open") BigDecimal open,
            @Param("high") BigDecimal high,
            @Param("low") BigDecimal low,
            @Param("volume") Long volume,
            @Param("value") Long value,
            @Param("marketCap") Long marketCap,
            @Param("source") String source,
            @Param("ingestedAt") Instant ingestedAt
    );
}
