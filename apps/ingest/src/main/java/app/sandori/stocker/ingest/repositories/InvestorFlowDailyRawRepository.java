package app.sandori.stocker.ingest.repositories;

import app.sandori.stocker.ingest.entities.InvestorFlowDailyRawEntity;
import app.sandori.stocker.ingest.entities.InvestorFlowDailyRawId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorFlowDailyRawRepository extends JpaRepository<InvestorFlowDailyRawEntity, InvestorFlowDailyRawId> {
}
