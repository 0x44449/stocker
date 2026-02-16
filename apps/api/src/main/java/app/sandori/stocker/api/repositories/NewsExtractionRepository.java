package app.sandori.stocker.api.repositories;

import app.sandori.stocker.api.entities.NewsExtractionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsExtractionRepository extends JpaRepository<NewsExtractionEntity, Long> {
}
