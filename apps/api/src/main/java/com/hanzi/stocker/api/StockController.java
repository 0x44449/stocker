package com.hanzi.stocker.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    @GetMapping
    public void search(@RequestParam String query) {
        // TODO: 종목 검색
    }

    @GetMapping("/{code}/situation")
    public void getSituation(
            @PathVariable String code,
            @RequestParam(required = false) LocalDate date) {
        // TODO: 종목 상황 요약 조회
    }

    @GetMapping("/{code}/prices")
    public void getPrices(
            @PathVariable String code,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        // TODO: 가격 데이터 조회
    }

    @GetMapping("/{code}/disclosures")
    public void getDisclosures(
            @PathVariable String code,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        // TODO: 공시 데이터 조회
    }
}
