package app.sandori.stocker.ingest.controller;

import app.sandori.stocker.ingest.krx.master.KrxMasterCrawlEngine;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/internal/crawl/stock-master")
public class StockMasterCrawlController {

    private final KrxMasterCrawlEngine engine;

    public StockMasterCrawlController(KrxMasterCrawlEngine engine) {
        this.engine = engine;
    }

    public record TriggerResponse(String status) {}

    @PostMapping
    public TriggerResponse trigger() {
        CompletableFuture.runAsync(engine::crawl);
        return new TriggerResponse("started");
    }
}
