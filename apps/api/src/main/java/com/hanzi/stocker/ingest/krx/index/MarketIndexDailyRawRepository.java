package com.hanzi.stocker.ingest.krx.index;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketIndexDailyRawRepository extends JpaRepository<MarketIndexDailyRawEntity, MarketIndexDailyRawId> {
}
