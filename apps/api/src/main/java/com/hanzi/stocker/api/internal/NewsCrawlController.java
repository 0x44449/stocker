package com.hanzi.stocker.api.internal;

import com.hanzi.stocker.ingest.news.NewsCrawlEngine;
import com.hanzi.stocker.ingest.news.NewsCrawlLock;
import com.hanzi.stocker.ingest.news.provider.ProviderRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/internal/crawl/news")
@Tag(name = "Internal - News Crawl", description = "뉴스 크롤링 관리 (내부용)")
public class NewsCrawlController {

    private final NewsCrawlEngine engine;
    private final ProviderRegistry providerRegistry;
    private final NewsCrawlLock crawlLock;

    public NewsCrawlController(NewsCrawlEngine engine, ProviderRegistry providerRegistry, NewsCrawlLock crawlLock) {
        this.engine = engine;
        this.providerRegistry = providerRegistry;
        this.crawlLock = crawlLock;
    }

    @Schema(description = "크롤링 트리거 응답")
    public record CrawlTriggerResponse(
            @Schema(description = "처리 상태", example = "started", allowableValues = {"started", "already_running", "not_found"})
            String status,
            @Schema(description = "프로바이더 ID", example = "hankyung")
            String provider
    ) {}

    @Schema(description = "크롤링 상태 응답")
    public record CrawlStatusResponse(
            @Schema(description = "프로바이더 ID", example = "hankyung")
            String provider,
            @Schema(description = "실행 상태", example = "idle", allowableValues = {"running", "idle"})
            String status
    ) {}

    @PostMapping("/{providerId}")
    @Operation(summary = "뉴스 크롤링 트리거", description = "특정 프로바이더의 크롤링을 수동으로 시작한다. 비동기로 실행되며 즉시 응답한다.")
    @ApiResponse(responseCode = "200", description = "트리거 결과",
            content = @Content(schema = @Schema(implementation = CrawlTriggerResponse.class)))
    public CrawlTriggerResponse trigger(
            @Parameter(description = "뉴스 프로바이더 ID", required = true, example = "hankyung")
            @PathVariable String providerId) {
        var provider = providerRegistry.get(providerId);
        if (provider.isEmpty()) {
            return new CrawlTriggerResponse("not_found", providerId);
        }

        if (!crawlLock.tryLock(providerId)) {
            return new CrawlTriggerResponse("already_running", providerId);
        }

        CompletableFuture.runAsync(() -> {
            try {
                engine.crawl(provider.get());
            } finally {
                crawlLock.unlock(providerId);
            }
        });

        return new CrawlTriggerResponse("started", providerId);
    }

    @GetMapping("/status")
    @Operation(summary = "크롤링 상태 조회", description = "전체 프로바이더별 크롤링 실행 상태를 조회한다")
    @ApiResponse(responseCode = "200", description = "상태 목록")
    public List<CrawlStatusResponse> status() {
        return providerRegistry.getAllIds().stream()
                .map(id -> new CrawlStatusResponse(id, crawlLock.isRunning(id) ? "running" : "idle"))
                .toList();
    }
}
