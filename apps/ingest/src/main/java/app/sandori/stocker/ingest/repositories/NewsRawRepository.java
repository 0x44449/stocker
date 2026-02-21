package app.sandori.stocker.ingest.repositories;

import app.sandori.stocker.ingest.entities.NewsRawEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NewsRawRepository extends JpaRepository<NewsRawEntity, Long> {

    boolean existsByUrl(String url);

    List<NewsRawEntity> findByPublishedAtBetweenAndImageKeyIsNull(LocalDateTime from, LocalDateTime to);
}
