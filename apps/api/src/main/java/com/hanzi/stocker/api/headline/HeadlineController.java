package com.hanzi.stocker.api.headline;

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

import java.time.LocalDate;

@RestController
@RequestMapping("/api/news")
@Tag(name = "News Headline", description = "헤드라인 뉴스 조회")
public class HeadlineController {

    private final HeadlineService service;

    public HeadlineController(HeadlineService service) {
        this.service = service;
    }

    @GetMapping("/headline")
    @Operation(summary = "헤드라인 뉴스 조회", description = "특정 날짜의 기업명 언급 횟수 기반 헤드라인 종목과 관련 기사를 조회한다")
    @ApiResponse(responseCode = "200", description = "헤드라인 목록",
            content = @Content(schema = @Schema(implementation = HeadlineService.HeadlineResponse.class)))
    public HeadlineService.HeadlineResponse headline(
            @Parameter(description = "조회 날짜", required = true, example = "2026-02-05")
            @RequestParam LocalDate date,
            @Parameter(description = "임계값 (이 횟수 이상 언급된 종목만 선정)", example = "5")
            @RequestParam(defaultValue = "5") int threshold) {
        return service.getHeadlines(date, threshold);
    }
}
