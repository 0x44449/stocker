package com.hanzi.stocker.repositories;

import com.hanzi.stocker.entities.NewsCompanyExtractionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsCompanyExtractionRepository extends JpaRepository<NewsCompanyExtractionEntity, Long> {

    Optional<NewsCompanyExtractionEntity> findByNewsId(Long newsId);
}
