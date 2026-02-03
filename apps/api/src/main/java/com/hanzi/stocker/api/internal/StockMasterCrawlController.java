package com.hanzi.stocker.api.internal;

import com.hanzi.stocker.ingest.krx.master.KrxMasterCrawlEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/internal/crawl/stock-master")
@Tag(name = "Internal - Stock Master Crawl", description = "종목 마스터 크롤링 관리 (내부용)")
public class StockMasterCrawlController {

    private final KrxMasterCrawlEngine engine;

    public StockMasterCrawlController(KrxMasterCrawlEngine engine) {
        this.engine = engine;
    }

    @Schema(description = "크롤링 트리거 응답")
    public record TriggerResponse(
            @Schema(description = "처리 상태", example = "started")
            String status
    ) {}

    @PostMapping
    @Operation(summary = "종목 마스터 크롤링 트리거", description = "KRX 종목 마스터 데이터 크롤링을 수동으로 시작한다. 비동기로 실행되며 즉시 응답한다.")
    @ApiResponse(responseCode = "200", description = "트리거 성공",
            content = @Content(schema = @Schema(implementation = TriggerResponse.class)))
    public TriggerResponse trigger() {
        CompletableFuture.runAsync(engine::crawl);
        return new TriggerResponse("started");
    }
}
