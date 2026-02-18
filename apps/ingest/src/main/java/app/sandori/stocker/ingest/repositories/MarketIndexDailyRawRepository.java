package app.sandori.stocker.ingest.repositories;

import app.sandori.stocker.ingest.entities.MarketIndexDailyRawEntity;
import app.sandori.stocker.ingest.entities.MarketIndexDailyRawId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketIndexDailyRawRepository extends JpaRepository<MarketIndexDailyRawEntity, MarketIndexDailyRawId> {
}
