package app.sandori.stocker.api.domain.newsmapping;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsStockManualMappingRepository extends JpaRepository<NewsStockManualMappingEntity, Long> {

    Optional<NewsStockManualMappingEntity> findByNewsId(Long newsId);

    void deleteByNewsId(Long newsId);

    boolean existsByNewsId(Long newsId);
}
