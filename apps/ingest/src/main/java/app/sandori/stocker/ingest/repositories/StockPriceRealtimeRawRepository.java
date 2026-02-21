package app.sandori.stocker.ingest.repositories;

import app.sandori.stocker.ingest.entities.StockPriceRealtimeRawEntity;
import app.sandori.stocker.ingest.entities.StockPriceRealtimeRawId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPriceRealtimeRawRepository extends JpaRepository<StockPriceRealtimeRawEntity, StockPriceRealtimeRawId> {
}
