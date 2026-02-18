package app.sandori.stocker.ingest.repositories;

import app.sandori.stocker.ingest.entities.StockPriceDailyRawEntity;
import app.sandori.stocker.ingest.entities.StockPriceDailyRawId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPriceDailyRawRepository extends JpaRepository<StockPriceDailyRawEntity, StockPriceDailyRawId> {
}
