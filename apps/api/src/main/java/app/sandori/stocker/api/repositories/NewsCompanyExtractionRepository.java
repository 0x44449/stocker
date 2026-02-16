package app.sandori.stocker.api.repositories;

import app.sandori.stocker.api.entities.NewsCompanyExtractionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsCompanyExtractionRepository extends JpaRepository<NewsCompanyExtractionEntity, Long> {

    Optional<NewsCompanyExtractionEntity> findByNewsId(Long newsId);
}
