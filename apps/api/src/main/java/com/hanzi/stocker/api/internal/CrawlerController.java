package com.hanzi.stocker.api.internal;

import com.hanzi.stocker.ingest.news.engine.NewsCrawlEngine;
import com.hanzi.stocker.ingest.news.provider.ProviderRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/internal/crawler")
public class CrawlerController {

    private final NewsCrawlEngine crawlEngine;
    private final ProviderRegistry providerRegistry;
    private final ExecutorService executor;

    public CrawlerController(NewsCrawlEngine crawlEngine, ProviderRegistry providerRegistry) {
        this.crawlEngine = crawlEngine;
        this.providerRegistry = providerRegistry;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @GetMapping("/providers")
    public List<String> listProviders() {
        return providerRegistry.getAllIds();
    }

    @PostMapping("/run/{providerId}")
    public ResponseEntity<NewsCrawlEngine.CrawlResult> run(@PathVariable String providerId) {
        return providerRegistry.get(providerId)
                .map(provider -> ResponseEntity.ok(crawlEngine.crawl(provider)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/run-all")
    public List<ProviderCrawlResult> runAll() {
        List<CompletableFuture<ProviderCrawlResult>> futures = providerRegistry.getAll().stream()
                .map(provider -> CompletableFuture.supplyAsync(
                        () -> new ProviderCrawlResult(provider.id(), crawlEngine.crawl(provider)),
                        executor
                ))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    public record ProviderCrawlResult(String providerId, NewsCrawlEngine.CrawlResult result) {}
}
