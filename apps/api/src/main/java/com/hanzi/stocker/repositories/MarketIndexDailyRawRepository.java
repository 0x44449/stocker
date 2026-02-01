package com.hanzi.stocker.repositories;

import com.hanzi.stocker.entities.MarketIndexDailyRawEntity;
import com.hanzi.stocker.entities.MarketIndexDailyRawId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketIndexDailyRawRepository extends JpaRepository<MarketIndexDailyRawEntity, MarketIndexDailyRawId> {
}
