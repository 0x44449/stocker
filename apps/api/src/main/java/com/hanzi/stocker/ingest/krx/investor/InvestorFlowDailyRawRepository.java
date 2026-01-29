package com.hanzi.stocker.ingest.krx.investor;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorFlowDailyRawRepository extends JpaRepository<InvestorFlowDailyRawEntity, InvestorFlowDailyRawId> {
}
