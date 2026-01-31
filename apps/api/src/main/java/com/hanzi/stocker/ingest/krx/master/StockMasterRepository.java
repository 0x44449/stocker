package com.hanzi.stocker.ingest.krx.master;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMasterRepository extends JpaRepository<StockMasterEntity, String> {
}
