package com.hanzi.stocker.ingest.news.config;

import com.hanzi.stocker.ingest.news.engine.NewsCrawlJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class CrawlSchedulerConfig {

    private static final Logger log = LoggerFactory.getLogger(CrawlSchedulerConfig.class);

    private final NewsCrawlJobService jobService;

    public CrawlSchedulerConfig(NewsCrawlJobService jobService) {
        this.jobService = jobService;
    }

    @Bean
    public TaskScheduler crawlTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("news-crawl-");
        scheduler.initialize();
        return scheduler;
    }

    @Scheduled(cron = "0 0 */2 * * *", scheduler = "crawlTaskScheduler")
    public void runScheduledCrawl() {
        log.info("Scheduled crawl triggered");
        jobService.runAll();
    }
}
