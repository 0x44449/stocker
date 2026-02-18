package app.sandori.stocker.api.domain;

import app.sandori.stocker.api.repositories.StockMasterRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@Tag(name = "Stock", description = "종목 정보 조회")
public class StockController {

    // 검색 결과 최대 건수
    private static final int SEARCH_MAX_RESULTS = 20;
    // 검색어 최소 길이
    private static final int SEARCH_MIN_LENGTH = 2;

    private final StockMasterRepository stockMasterRepository;

    public StockController(StockMasterRepository stockMasterRepository) {
        this.stockMasterRepository = stockMasterRepository;
    }

    public record StockSearchDto(
            String stockCode,
            String nameKr,
            String nameKrShort,
            String market
    ) {}

    @GetMapping
    @Operation(summary = "종목 검색", description = "종목명 또는 종목코드로 검색한다. 검색어 2자 이상, 최대 20건 반환.")
    @ApiResponse(responseCode = "200", description = "검색 결과")
    public List<StockSearchDto> search(
            @Parameter(description = "검색어 (종목명 또는 종목코드)", required = true, example = "삼성전자")
            @RequestParam String query) {
        if (query == null || query.trim().length() < SEARCH_MIN_LENGTH) {
            return List.of();
        }

        return stockMasterRepository.searchByQuery(query.trim(), PageRequest.of(0, SEARCH_MAX_RESULTS))
                .stream()
                .map(e -> new StockSearchDto(e.getStockCode(), e.getNameKr(), e.getNameKrShort(), e.getMarket()))
                .toList();
    }
}
