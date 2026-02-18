package app.sandori.stocker.ingest.krx.stock;

import app.sandori.stocker.ingest.entities.StockPriceDailyRawEntity;
import app.sandori.stocker.ingest.repositories.StockPriceDailyRawRepository;
import app.sandori.stocker.ingest.krx.common.KrxFileClient;
import app.sandori.stocker.ingest.krx.common.KrxSessionProvider;
import org.apache.commons.csv.CSVFormat;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Component
public class KrxStockCrawlEngine {

    private static final String REFERER = "https://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201020101";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final KrxSessionProvider sessionProvider;
    private final KrxFileClient fileClient;
    private final StockPriceDailyRawRepository repository;

    public KrxStockCrawlEngine(KrxSessionProvider sessionProvider, KrxFileClient fileClient, StockPriceDailyRawRepository repository) {
        this.sessionProvider = sessionProvider;
        this.fileClient = fileClient;
        this.repository = repository;
    }

    public void crawl(LocalDate date, String market) {
        var formData = new LinkedMultiValueMap<String, String>();
        formData.add("locale", "ko_KR");
        formData.add("mktId", "STK");
        formData.add("trdDd", date.format(DATE_FORMAT));
        formData.add("share", "1");
        formData.add("money", "1");
        formData.add("csvxls_isNo", "false");
        formData.add("name", "fileDown");
        formData.add("url", "dbms/MDC/STAT/standard/MDCSTAT01501");

        var csvBytes = fileClient.download(sessionProvider.get(), REFERER, formData);

        try (var reader = new InputStreamReader(new ByteArrayInputStream(csvBytes), Charset.forName("EUC-KR"))) {
            var parser = CSVFormat.Builder.create()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .build()
                    .parse(reader);
            var entities = new ArrayList<StockPriceDailyRawEntity>();
            for (var record : parser) {
                var entity = new StockPriceDailyRawEntity(
                        date,
                        market,
                        record.get("종목코드"),
                        record.get("종목명"),
                        parseLong(record.get("종가")),
                        parseLong(record.get("대비")),
                        parseBigDecimal(record.get("등락률")),
                        parseLong(record.get("시가")),
                        parseLong(record.get("고가")),
                        parseLong(record.get("저가")),
                        parseLong(record.get("거래량")),
                        parseLong(record.get("거래대금")),
                        parseLong(record.get("시가총액")),
                        parseLong(record.get("상장주식수")),
                        "KRX"
                );
                entities.add(entity);
            }
            repository.saveAll(entities);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse KRX stock price CSV", e);
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value);
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.parseLong(value);
    }
}
