package app.sandori.stocker.api.repositories;

import app.sandori.stocker.api.entities.StockMasterEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockMasterRepository extends JpaRepository<StockMasterEntity, String> {

    /**
     * 종목 검색. stock_code, name_kr, name_kr_short에서 검색어를 포함하는 종목을 조회한다.
     */
    @Query("SELECT s FROM StockMasterEntity s WHERE " +
            "LOWER(s.stockCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.nameKr) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.nameKrShort) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<StockMasterEntity> searchByQuery(@Param("query") String query, Pageable pageable);

    boolean existsByStockCode(String stockCode);

    List<StockMasterEntity> findByStockCodeIn(List<String> stockCodes);
}
