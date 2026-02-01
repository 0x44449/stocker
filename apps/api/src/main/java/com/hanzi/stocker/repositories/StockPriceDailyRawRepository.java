package com.hanzi.stocker.repositories;

import com.hanzi.stocker.entities.StockPriceDailyRawEntity;
import com.hanzi.stocker.entities.StockPriceDailyRawId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPriceDailyRawRepository extends JpaRepository<StockPriceDailyRawEntity, StockPriceDailyRawId> {
}
