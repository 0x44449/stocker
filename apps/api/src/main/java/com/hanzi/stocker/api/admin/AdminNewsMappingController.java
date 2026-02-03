package com.hanzi.stocker.api.admin;

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
@RequestMapping("/api/admin/news-mappings")
@Tag(name = "Admin - News Mapping", description = "뉴스-종목 매핑 관리 (관리자용)")
public class AdminNewsMappingController {

    private final AdminNewsMappingService service;

    public AdminNewsMappingController(AdminNewsMappingService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "뉴스 매핑 목록 조회", description = "뉴스별 종목 매핑 상태를 조회한다")
    @ApiResponse(responseCode = "200", description = "매핑 목록",
            content = @Content(schema = @Schema(implementation = AdminNewsMappingService.NewsMappingListResponse.class)))
    public AdminNewsMappingService.NewsMappingListResponse list(
            @Parameter(description = "필터 (all: 전체, mapped: 매핑됨, unmapped: 미매핑)", example = "all")
            @RequestParam(defaultValue = "all") String filter,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "검색어 (제목 검색)", example = "삼성")
            @RequestParam(defaultValue = "") String search
    ) {
        return service.getList(filter, page, size, search);
    }
}
