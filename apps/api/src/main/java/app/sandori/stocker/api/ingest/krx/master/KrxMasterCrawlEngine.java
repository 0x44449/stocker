package app.sandori.stocker.api.ingest.krx.master;

import app.sandori.stocker.api.entities.StockMasterEntity;
import app.sandori.stocker.api.repositories.StockMasterRepository;
import app.sandori.stocker.api.ingest.krx.common.KrxFileClient;
import app.sandori.stocker.api.ingest.krx.common.KrxSessionProvider;
import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Component
public class KrxMasterCrawlEngine {

    private static final Logger log = LoggerFactory.getLogger(KrxMasterCrawlEngine.class);
    private static final String REFERER = "https://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201020201";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final KrxSessionProvider sessionProvider;
    private final KrxFileClient fileClient;
    private final StockMasterRepository repository;

    public KrxMasterCrawlEngine(KrxSessionProvider sessionProvider, KrxFileClient fileClient, StockMasterRepository repository) {
        this.sessionProvider = sessionProvider;
        this.fileClient = fileClient;
        this.repository = repository;
    }

    @Transactional
    public void crawl() {
        log.info("종목 마스터 크롤링 시작");

        var formData = new LinkedMultiValueMap<String, String>();
        formData.add("locale", "ko_KR");
        formData.add("mktId", "STK");
        formData.add("share", "1");
        formData.add("csvxls_isNo", "false");
        formData.add("name", "fileDown");
        formData.add("url", "dbms/MDC/STAT/standard/MDCSTAT01901");

        try {
            var csvBytes = fileClient.download(sessionProvider.get(), REFERER, formData);
            log.info("CSV 다운로드 완료 - {} bytes", csvBytes.length);

            try (var reader = new InputStreamReader(new ByteArrayInputStream(csvBytes), Charset.forName("EUC-KR"))) {
                var parser = CSVFormat.Builder.create()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreEmptyLines(true)
                        .build()
                        .parse(reader);

                var entities = new ArrayList<StockMasterEntity>();
                for (var record : parser) {
                    entities.add(new StockMasterEntity(
                            record.get("표준코드"),
                            record.get("단축코드"),
                            record.get("한글 종목명"),
                            record.get("한글 종목약명"),
                            record.get("영문 종목명"),
                            parseDate(record.get("상장일")),
                            record.get("시장구분"),
                            record.get("증권구분"),
                            blankToNull(record.get("소속부")),
                            record.get("주식종류"),
                            // 액면가: 투자회사/펀드 등은 "무액면"으로 표기되어 숫자가 아닌 값은 null 처리
                            parseLongOrNull(record.get("액면가")),
                            parseLong(record.get("상장주식수"))
                    ));
                }
                log.info("CSV 파싱 완료 - {}건", entities.size());

                repository.saveAll(entities);
                log.info("종목 마스터 크롤링 완료 - {}건 저장", entities.size());
            }
        } catch (Exception e) {
            log.error("종목 마스터 크롤링 실패", e);
            throw new RuntimeException("종목 마스터 크롤링 실패", e);
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value, DATE_FORMAT);
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.parseLong(value.replace(",", ""));
    }

    /**
     * 숫자가 아닌 값은 null로 처리하는 느슨한 버전.
     * 특수 케이스(예: 액면가의 "무액면")에만 사용.
     */
    private Long parseLongOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
