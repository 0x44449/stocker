package app.sandori.stocker.ingest.repositories;

import app.sandori.stocker.ingest.entities.StockMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMasterRepository extends JpaRepository<StockMasterEntity, String> {
}
