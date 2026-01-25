package com.hanzi.stocker.api.internal;

import com.hanzi.stocker.ingest.news.engine.NewsCrawlJobService;
import com.hanzi.stocker.ingest.news.provider.ProviderRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/crawler")
public class CrawlerController {

    private final NewsCrawlJobService jobService;
    private final ProviderRegistry providerRegistry;

    public CrawlerController(NewsCrawlJobService jobService, ProviderRegistry providerRegistry) {
        this.jobService = jobService;
        this.providerRegistry = providerRegistry;
    }

    @GetMapping("/providers")
    public List<String> listProviders() {
        return providerRegistry.getAllIds();
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("running", jobService.isRunning());
    }

    @PostMapping("/run/{providerId}")
    public ResponseEntity<NewsCrawlJobService.ProviderCrawlResult> run(@PathVariable String providerId) {
        if (providerRegistry.get(providerId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(jobService.runSingle(providerId));
    }

    @PostMapping("/run-all")
    public List<NewsCrawlJobService.ProviderCrawlResult> runAll() {
        return jobService.runAll();
    }
}
