package com.hanzi.stocker.ingest.krx.investor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;

public interface InvestorFlowDailyRawRepository extends JpaRepository<InvestorFlowDailyRaw, InvestorFlowDailyRawId> {

    @Modifying
    @Query(value = """
            INSERT INTO investor_flow_daily_raw
            (trd_dd, market, investor_name, sell_volume, buy_volume, net_volume,
             sell_value, buy_value, net_value, source, ingested_at)
            VALUES (:trdDd, :market, :investorName, :sellVolume, :buyVolume, :netVolume,
                    :sellValue, :buyValue, :netValue, :source, :ingestedAt)
            ON CONFLICT (trd_dd, market, investor_name) DO UPDATE SET
                sell_volume = EXCLUDED.sell_volume,
                buy_volume = EXCLUDED.buy_volume,
                net_volume = EXCLUDED.net_volume,
                sell_value = EXCLUDED.sell_value,
                buy_value = EXCLUDED.buy_value,
                net_value = EXCLUDED.net_value,
                ingested_at = EXCLUDED.ingested_at
            """, nativeQuery = true)
    void upsert(
            @Param("trdDd") LocalDate trdDd,
            @Param("market") String market,
            @Param("investorName") String investorName,
            @Param("sellVolume") Long sellVolume,
            @Param("buyVolume") Long buyVolume,
            @Param("netVolume") Long netVolume,
            @Param("sellValue") Long sellValue,
            @Param("buyValue") Long buyValue,
            @Param("netValue") Long netValue,
            @Param("source") String source,
            @Param("ingestedAt") Instant ingestedAt
    );
}
