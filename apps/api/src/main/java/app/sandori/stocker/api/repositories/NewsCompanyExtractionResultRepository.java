package app.sandori.stocker.api.repositories;

import app.sandori.stocker.api.entities.NewsCompanyExtractionResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsCompanyExtractionResultRepository extends JpaRepository<NewsCompanyExtractionResultEntity, Long> {

    List<NewsCompanyExtractionResultEntity> findByExtractionId(Long extractionId);

    List<NewsCompanyExtractionResultEntity> findByExtractionIdIn(List<Long> extractionIds);
}
