package app.sandori.stocker.api.repositories;

import app.sandori.stocker.api.entities.StockPriceDailyRawEntity;
import app.sandori.stocker.api.entities.StockPriceDailyRawId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPriceDailyRawRepository extends JpaRepository<StockPriceDailyRawEntity, StockPriceDailyRawId> {
}
