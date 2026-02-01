package com.hanzi.stocker.repositories;

import com.hanzi.stocker.entities.NewsRawEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRawRepository extends JpaRepository<NewsRawEntity, Long> {

    boolean existsByUrl(String url);
}
