package app.sandori.stocker.api.domain.watchlist;

import app.sandori.stocker.api.config.Authenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Authenticated
@RestController
@RequestMapping("/api/watchlist")
@Tag(name = "Watchlist", description = "관심종목 관리")
public class WatchlistController {

    private final WatchlistService service;

    public WatchlistController(WatchlistService service) {
        this.service = service;
    }

    record AddStockRequest(String stockCode) {}

    @GetMapping
    @Operation(summary = "관심종목 목록 조회")
    public WatchlistService.WatchlistResponse getWatchlist(@RequestAttribute("uid") String uid) {
        return service.getWatchlist(uid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "관심종목 추가")
    public void addStock(@RequestAttribute("uid") String uid, @RequestBody AddStockRequest request) {
        service.addStock(uid, request.stockCode());
    }

    @DeleteMapping("/{stockCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "관심종목 삭제")
    public void removeStock(@RequestAttribute("uid") String uid, @PathVariable String stockCode) {
        service.removeStock(uid, stockCode);
    }
}
