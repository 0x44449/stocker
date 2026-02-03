package com.hanzi.stocker.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/stocks")
@Tag(name = "Stock", description = "종목 정보 조회")
public class StockController {

    @GetMapping
    @Operation(summary = "종목 검색", description = "종목명 또는 종목코드로 검색한다")
    @ApiResponse(responseCode = "200", description = "검색 결과")
    public void search(
            @Parameter(description = "검색어 (종목명 또는 종목코드)", required = true, example = "삼성전자")
            @RequestParam String query) {
        // TODO: 종목 검색
    }

    @GetMapping("/{code}/situation")
    @Operation(summary = "종목 상황 요약 조회", description = "특정 종목의 현재 상황 요약 정보를 조회한다")
    @ApiResponse(responseCode = "200", description = "상황 요약")
    @ApiResponse(responseCode = "404", description = "종목 없음")
    public void getSituation(
            @Parameter(description = "종목코드", required = true, example = "005930")
            @PathVariable String code,
            @Parameter(description = "조회 기준일 (미입력시 오늘)")
            @RequestParam(required = false) LocalDate date) {
        // TODO: 종목 상황 요약 조회
    }

    @GetMapping("/{code}/prices")
    @Operation(summary = "가격 데이터 조회", description = "특정 종목의 가격 이력을 조회한다")
    @ApiResponse(responseCode = "200", description = "가격 데이터")
    @ApiResponse(responseCode = "404", description = "종목 없음")
    public void getPrices(
            @Parameter(description = "종목코드", required = true, example = "005930")
            @PathVariable String code,
            @Parameter(description = "조회 시작일")
            @RequestParam(required = false) LocalDate from,
            @Parameter(description = "조회 종료일")
            @RequestParam(required = false) LocalDate to) {
        // TODO: 가격 데이터 조회
    }

    @GetMapping("/{code}/disclosures")
    @Operation(summary = "공시 데이터 조회", description = "특정 종목의 공시 목록을 조회한다")
    @ApiResponse(responseCode = "200", description = "공시 목록")
    @ApiResponse(responseCode = "404", description = "종목 없음")
    public void getDisclosures(
            @Parameter(description = "종목코드", required = true, example = "005930")
            @PathVariable String code,
            @Parameter(description = "조회 시작일")
            @RequestParam(required = false) LocalDate from,
            @Parameter(description = "조회 종료일")
            @RequestParam(required = false) LocalDate to) {
        // TODO: 공시 데이터 조회
    }
}
