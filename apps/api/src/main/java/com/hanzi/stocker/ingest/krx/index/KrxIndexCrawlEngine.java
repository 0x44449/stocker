package com.hanzi.stocker.ingest.krx.index;

import com.hanzi.stocker.ingest.krx.common.KrxAuth;
import com.hanzi.stocker.ingest.krx.common.KrxCsvParser;
import com.hanzi.stocker.ingest.krx.common.KrxFileClient;
import org.springframework.util.LinkedMultiValueMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class KrxIndexCrawlEngine {

    private static final String REFERER = "https://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201020101";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final KrxAuth.LoginSession session;
    private final KrxFileClient fileClient;
    private final KrxCsvParser csvParser;

    public KrxIndexCrawlEngine(
            KrxFileClient fileClient,
            KrxAuth.LoginSession session,
            KrxCsvParser csvParser
    ) {
        this.fileClient = fileClient;
        this.session = session;
        this.csvParser = csvParser;
    }

    public void crawl(LocalDate trdDd) {
        var formData = new LinkedMultiValueMap<String, String>();
        formData.add("locale", "ko_KR");
        formData.add("idxIndMidclssCd", "02");
        formData.add("trdDd", trdDd.format(DATE_FORMAT));
        formData.add("share", "1");
        formData.add("money", "1");
        formData.add("csvxls_isNo", "false");
        formData.add("name", "fileDown");
        formData.add("url", "dbms/MDC/STAT/standard/MDCSTAT00101");

        var csvBytes = fileClient.download(session, REFERER, formData);

        var columns = Arrays.asList(
            "Close",
            "Diff",
            "DiffRate",
            "Open",
            "High",
            "Low",
            "Volume",
            "Value",
            "MarketCap"
        );
        var rows = csvParser.parse(csvBytes, columns);
    }
}
