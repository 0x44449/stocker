package com.hanzi.stocker.repositories;

import com.hanzi.stocker.entities.StockClusterResultEntity;
import com.hanzi.stocker.entities.StockClusterResultId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockClusterResultRepository extends JpaRepository<StockClusterResultEntity, StockClusterResultId> {

    StockClusterResultEntity findFirstByStockNameOrderByClusteredAtDesc(String stockName);
}
