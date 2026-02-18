package app.sandori.stocker.api.domain.feed;

import app.sandori.stocker.api.config.Authenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Authenticated
@RestController
@RequestMapping("/api/feed")
@Tag(name = "Feed", description = "피드 카드 조회")
public class HotStockController {

    private final HotStockService service;

    public HotStockController(HotStockService service) {
        this.service = service;
    }

    @GetMapping("/hot-stocks")
    @Operation(summary = "오늘의 핫 종목", description = "오늘 뉴스에서 가장 많이 언급된 종목 상위 3개를 반환한다")
    @ApiResponse(responseCode = "200", description = "핫 종목 목록",
            content = @Content(schema = @Schema(implementation = HotStockService.HotStockResponse.class)))
    public HotStockService.HotStockResponse hotStocks() {
        return service.getHotStocks();
    }
}
