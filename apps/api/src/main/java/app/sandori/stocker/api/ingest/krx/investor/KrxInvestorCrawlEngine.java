package app.sandori.stocker.api.ingest.krx.investor;

import app.sandori.stocker.api.entities.InvestorFlowDailyRawEntity;
import app.sandori.stocker.api.repositories.InvestorFlowDailyRawRepository;
import app.sandori.stocker.api.ingest.krx.common.KrxFileClient;
import app.sandori.stocker.api.ingest.krx.common.KrxSessionProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Component
public class KrxInvestorCrawlEngine {

    private static final String REFERER = "https://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201020101";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final KrxSessionProvider sessionProvider;
    private final KrxFileClient fileClient;
    private final InvestorFlowDailyRawRepository repository;

    public KrxInvestorCrawlEngine(KrxSessionProvider sessionProvider, KrxFileClient fileClient, InvestorFlowDailyRawRepository repository) {
        this.sessionProvider = sessionProvider;
        this.fileClient = fileClient;
        this.repository = repository;
    }

    public void crawl(LocalDate date, String market) {
        var formData = new LinkedMultiValueMap<String, String>();
        formData.add("locale", "ko_KR");
        formData.add("inqTpCd", "1");
        formData.add("trdVolVal", "2");
        formData.add("askBid", "3");
        formData.add("mktId", market);
        formData.add("etf", "EF");
        formData.add("etn", "EN");
        formData.add("elw", "EW");
        formData.add("strtDd", date.format(DATE_FORMAT));
        formData.add("endDd", date.format(DATE_FORMAT));
        formData.add("share", "1");
        formData.add("money", "1");
        formData.add("csvxls_isNo", "false");
        formData.add("name", "fileDown");
        formData.add("url", "dbms/MDC/STAT/standard/MDCSTAT02201");

        var csvBytes = fileClient.download(sessionProvider.get(), REFERER, formData);

        // 투자자구분,거래량_매도,거래량_매수,거래량_순매수,거래대금_매도,거래대금_매수,거래대금_순매수
        try (var reader = new InputStreamReader(new ByteArrayInputStream(csvBytes), Charset.forName("EUC-KR"))) {
            var parser = org.apache.commons.csv.CSVFormat.Builder.create()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .build()
                    .parse(reader);
            var entities = new ArrayList<InvestorFlowDailyRawEntity>();
            for (var record : parser) {
                var entity = new InvestorFlowDailyRawEntity(
                        date,
                        market,
                        record.get("투자자구분"),
                        parseLong(record.get("거래량_매도")),
                        parseLong(record.get("거래량_매수")),
                        parseLong(record.get("거래량_순매수")),
                        parseLong(record.get("거래대금_매도")),
                        parseLong(record.get("거래대금_매수")),
                        parseLong(record.get("거래대금_순매수")),
                        "KRX"
                );
                entities.add(entity);
            }
            repository.saveAll(entities);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse KRX Investor Flow CSV", e);
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.parseLong(value);
    }
}
