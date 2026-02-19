package app.sandori.stocker.api.domain.watchlist;

import app.sandori.stocker.api.entities.UserWatchlistEntity;
import app.sandori.stocker.api.repositories.StockMasterRepository;
import app.sandori.stocker.api.repositories.UserWatchlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WatchlistService {

    private final UserWatchlistRepository watchlistRepository;
    private final StockMasterRepository stockMasterRepository;

    public WatchlistService(UserWatchlistRepository watchlistRepository, StockMasterRepository stockMasterRepository) {
        this.watchlistRepository = watchlistRepository;
        this.stockMasterRepository = stockMasterRepository;
    }

    // --- DTO ---

    public record WatchlistItem(String stockCode, String stockName, int sortOrder, OffsetDateTime addedAt) {}

    public record WatchlistResponse(List<WatchlistItem> stocks) {}

    /**
     * 관심종목 목록 조회.
     */
    public WatchlistResponse getWatchlist(String uid) {
        var watchlist = watchlistRepository.findByUserIdOrderBySortOrder(uid);
        if (watchlist.isEmpty()) {
            return new WatchlistResponse(List.of());
        }

        // 종목코드 → 종목명 매핑
        var stockCodes = watchlist.stream().map(UserWatchlistEntity::getStockCode).toList();
        Map<String, String> codeToName = stockMasterRepository.findByStockCodeIn(stockCodes).stream()
                .collect(Collectors.toMap(
                        s -> s.getStockCode(),
                        s -> s.getNameKrShort()
                ));

        var items = watchlist.stream()
                .map(w -> new WatchlistItem(
                        w.getStockCode(),
                        codeToName.getOrDefault(w.getStockCode(), w.getStockCode()),
                        w.getSortOrder(),
                        w.getAddedAt()
                ))
                .toList();

        return new WatchlistResponse(items);
    }

    /**
     * 관심종목 추가.
     */
    @Transactional
    public void addStock(String uid, String stockCode) {
        // 종목코드 유효성 확인
        if (!stockMasterRepository.existsByStockCode(stockCode)) {
            throw new IllegalArgumentException("존재하지 않는 종목코드: " + stockCode);
        }

        // 중복 확인
        if (watchlistRepository.findByUserIdAndStockCode(uid, stockCode).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 관심종목: " + stockCode);
        }

        // 다음 sortOrder 계산
        int nextOrder = watchlistRepository.countByUserId(uid);
        watchlistRepository.save(new UserWatchlistEntity(uid, stockCode, nextOrder));
    }

    /**
     * 관심종목 삭제.
     */
    @Transactional
    public void removeStock(String uid, String stockCode) {
        if (watchlistRepository.findByUserIdAndStockCode(uid, stockCode).isEmpty()) {
            throw new IllegalArgumentException("등록되지 않은 관심종목: " + stockCode);
        }
        watchlistRepository.deleteByUserIdAndStockCode(uid, stockCode);
    }
}
