package com.hanzi.stocker.ingest.krx.index;

import com.hanzi.stocker.ingest.krx.common.KrxSessionProvider;

import java.time.LocalDate;

public class KrxIndexCrawlService {

    private static final String PROVIDER_ID = "krx-index";

    public KrxIndexCrawlService() {}

    public void run() {
        var engine = new KrxIndexCrawlEngine();
        engine.crawl(LocalDate.now());
    }
}
