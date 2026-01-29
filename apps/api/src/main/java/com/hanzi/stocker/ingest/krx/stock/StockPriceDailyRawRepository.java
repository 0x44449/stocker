package com.hanzi.stocker.ingest.krx.stock;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPriceDailyRawRepository extends JpaRepository<StockPriceDailyRawEntity, StockPriceDailyRawId> {
}
