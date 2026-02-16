package app.sandori.stocker.api.repositories;

import app.sandori.stocker.api.entities.StockClusterResultEntity;
import app.sandori.stocker.api.entities.StockClusterResultId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockClusterResultRepository extends JpaRepository<StockClusterResultEntity, StockClusterResultId> {

    StockClusterResultEntity findFirstByStockNameOrderByClusteredAtDesc(String stockName);
}
