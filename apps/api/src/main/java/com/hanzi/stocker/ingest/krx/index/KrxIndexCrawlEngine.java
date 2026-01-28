package com.hanzi.stocker.ingest.krx.index;

import com.hanzi.stocker.ingest.krx.common.KrxFileClient;
import com.hanzi.stocker.ingest.krx.common.KrxSession;
import com.hanzi.stocker.ingest.krx.common.KrxSessionProvider;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.util.LinkedMultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class KrxIndexCrawlEngine {

    private static final String REFERER = "https://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201020101";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public KrxIndexCrawlEngine() {}

    public void crawl(LocalDate date) {
        var formData = new LinkedMultiValueMap<String, String>();
        formData.add("locale", "ko_KR");
        formData.add("idxIndMidclssCd", "02");
        formData.add("trdDd", date.format(DATE_FORMAT));
        formData.add("share", "1");
        formData.add("money", "1");
        formData.add("csvxls_isNo", "false");
        formData.add("name", "fileDown");
        formData.add("url", "dbms/MDC/STAT/standard/MDCSTAT00101");

        var fileClient = new KrxFileClient();
        var csvBytes = fileClient.download(KrxSessionProvider.get(), REFERER, formData);

        // 지수명,종가,대비,등락률,시가,고가,저가,거래량,거래대금,상장시가총액
        // "코스피","4990.07","37.54","0.76","4984.08","5021.13","4926.22","611779504","30014732983122","4124568849042154"
        try (var reader = new InputStreamReader(new ByteArrayInputStream(csvBytes), Charset.forName("EUC-KR"))) {
            CSVParser parser = CSVFormat.Builder.create()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .build()
                    .parse(reader);
            for (CSVRecord record : parser) {
                String indexName = record.get("지수명");
                String closeStr = record.get("종가");
                String diffStr = record.get("대비");
                String diffRateStr = record.get("등락률");
                String openStr = record.get("시가");
                String highStr = record.get("고가");
                String lowStr = record.get("저가");
                String volumeStr = record.get("거래량");
                String valueStr = record.get("거래대금");
                String marketCapStr = record.get("상장시가총액");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse KRX index CSV", e);
        }
    }
}
