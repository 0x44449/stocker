package app.sandori.stocker.ingest.repositories;

import app.sandori.stocker.ingest.entities.NewsRawEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRawRepository extends JpaRepository<NewsRawEntity, Long> {

    boolean existsByUrl(String url);
}
