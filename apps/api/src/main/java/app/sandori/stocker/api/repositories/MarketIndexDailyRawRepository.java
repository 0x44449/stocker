package app.sandori.stocker.api.repositories;

import app.sandori.stocker.api.entities.MarketIndexDailyRawEntity;
import app.sandori.stocker.api.entities.MarketIndexDailyRawId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketIndexDailyRawRepository extends JpaRepository<MarketIndexDailyRawEntity, MarketIndexDailyRawId> {
}
