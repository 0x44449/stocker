package com.hanzi.stocker.api;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    @GetMapping
    public void getWatchlist() {
        // TODO: 관심종목 목록 조회
    }

    @PostMapping("/{code}")
    public void addToWatchlist(@PathVariable String code) {
        // TODO: 관심종목 추가
    }

    @DeleteMapping("/{code}")
    public void removeFromWatchlist(@PathVariable String code) {
        // TODO: 관심종목 삭제
    }
}
