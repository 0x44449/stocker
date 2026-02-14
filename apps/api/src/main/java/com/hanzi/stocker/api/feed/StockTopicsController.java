package com.hanzi.stocker.api.feed;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
@Tag(name = "Feed", description = "피드 카드 조회")
public class StockTopicsController {

    private final StockTopicsService service;

    public StockTopicsController(StockTopicsService service) {
        this.service = service;
    }

    @GetMapping("/stock-topics")
    @Operation(summary = "종목별 뉴스 클러스터링", description = "특정 종목 관련 뉴스 클러스터링 결과를 DB에서 조회하여 반환한다")
    @ApiResponse(responseCode = "200", description = "클러스터링 결과",
            content = @Content(schema = @Schema(implementation = StockTopicsService.StockTopicsDto.class)))
    public StockTopicsService.StockTopicsDto stockTopics(
            @Parameter(description = "종목명") @RequestParam String keyword
    ) {
        return service.getStockTopics(keyword);
    }
}
