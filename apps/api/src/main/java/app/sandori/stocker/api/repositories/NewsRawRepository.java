package app.sandori.stocker.api.repositories;

import app.sandori.stocker.api.entities.NewsRawEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRawRepository extends JpaRepository<NewsRawEntity, Long> {

    boolean existsByUrl(String url);
}
