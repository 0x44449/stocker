package com.hanzi.stocker.repositories;

import com.hanzi.stocker.entities.InvestorFlowDailyRawEntity;
import com.hanzi.stocker.entities.InvestorFlowDailyRawId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorFlowDailyRawRepository extends JpaRepository<InvestorFlowDailyRawEntity, InvestorFlowDailyRawId> {
}
