package com.hanzi.stocker.repositories;

import com.hanzi.stocker.entities.NewsExtractionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsExtractionRepository extends JpaRepository<NewsExtractionEntity, Long> {
}
