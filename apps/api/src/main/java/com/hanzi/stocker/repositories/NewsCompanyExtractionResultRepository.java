package com.hanzi.stocker.repositories;

import com.hanzi.stocker.entities.NewsCompanyExtractionResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsCompanyExtractionResultRepository extends JpaRepository<NewsCompanyExtractionResultEntity, Long> {

    List<NewsCompanyExtractionResultEntity> findByExtractionId(Long extractionId);

    List<NewsCompanyExtractionResultEntity> findByExtractionIdIn(List<Long> extractionIds);
}
