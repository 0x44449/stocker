package app.sandori.stocker.api.repositories;

import app.sandori.stocker.api.entities.StockPriceDailyRawEntity;
import app.sandori.stocker.api.entities.StockPriceDailyRawId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockPriceDailyRawRepository extends JpaRepository<StockPriceDailyRawEntity, StockPriceDailyRawId> {

    /** 종목의 최근 거래일 주가 1건 */
    StockPriceDailyRawEntity findFirstByStockCodeOrderByTrdDdDesc(String stockCode);

    /** 여러 종목의 최근 거래일 주가 일괄 조회용 */
    List<StockPriceDailyRawEntity> findByStockCodeInOrderByTrdDdDesc(List<String> stockCodes);
}
