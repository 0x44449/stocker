package app.sandori.stocker.api.repositories;

import app.sandori.stocker.api.entities.UserWatchlistEntity;
import app.sandori.stocker.api.entities.UserWatchlistId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserWatchlistRepository extends JpaRepository<UserWatchlistEntity, UserWatchlistId> {

    List<UserWatchlistEntity> findByUserIdOrderBySortOrder(String userId);

    int countByUserId(String userId);

    Optional<UserWatchlistEntity> findByUserIdAndStockCode(String userId, String stockCode);

    void deleteByUserIdAndStockCode(String userId, String stockCode);
}
