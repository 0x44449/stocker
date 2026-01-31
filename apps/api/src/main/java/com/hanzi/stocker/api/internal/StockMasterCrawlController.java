package com.hanzi.stocker.api.internal;

import com.hanzi.stocker.ingest.krx.master.KrxMasterCrawlEngine;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/internal/crawl/stock-master")
public class StockMasterCrawlController {

    private final KrxMasterCrawlEngine engine;

    public StockMasterCrawlController(KrxMasterCrawlEngine engine) {
        this.engine = engine;
    }

    @PostMapping
    public Map<String, String> trigger() {
        CompletableFuture.runAsync(engine::crawl);
        return Map.of("status", "started");
    }
}
