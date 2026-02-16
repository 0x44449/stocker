package app.sandori.stocker.api.repositories;

import app.sandori.stocker.api.entities.InvestorFlowDailyRawEntity;
import app.sandori.stocker.api.entities.InvestorFlowDailyRawId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorFlowDailyRawRepository extends JpaRepository<InvestorFlowDailyRawEntity, InvestorFlowDailyRawId> {
}
