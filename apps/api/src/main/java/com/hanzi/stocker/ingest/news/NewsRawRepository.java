package com.hanzi.stocker.ingest.news;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRawRepository extends JpaRepository<NewsRawEntity, Long> {

    boolean existsByUrl(String url);
}
