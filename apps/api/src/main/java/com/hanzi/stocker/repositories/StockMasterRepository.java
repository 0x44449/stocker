package com.hanzi.stocker.repositories;

import com.hanzi.stocker.entities.StockMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMasterRepository extends JpaRepository<StockMasterEntity, String> {
}
