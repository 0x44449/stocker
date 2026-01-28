package com.hanzi.stocker.ingest.krx.investor;

import com.hanzi.stocker.ingest.krx.common.KrxFileClient;
import com.hanzi.stocker.ingest.krx.common.KrxSession;
import org.springframework.util.LinkedMultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class KrxInvestorCrawlEngine {

    private static final String REFERER = "https://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201020101";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final KrxSession session;
    private final KrxFileClient fileClient;

    public KrxInvestorCrawlEngine(
            KrxFileClient fileClient,
            KrxSession session
    ) {
        this.fileClient = fileClient;
        this.session = session;
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

        var csvBytes = fileClient.download(session, REFERER, formData);

        // 투자자구분,거래량_매도,거래량_매수,거래량_순매수,거래대금_매도,거래대금_매수,거래대금_순매수
        // "개인","1716581206","1748578011","31996805","19277216604858","19060194839120","-217021765738"
        /** investor_name = "개인"
         sell_volume = 1716581206
         buy_volume = 1748578011
         net_volume = 31996805
         sell_value = 19277216604858
         buy_value = 19060194839120
         net_value = -217021765738
         */
        try (var reader = new InputStreamReader(new ByteArrayInputStream(csvBytes), Charset.forName("EUC-KR"))) {
            var parser = org.apache.commons.csv.CSVFormat.Builder.create()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .build()
                    .parse(reader);
            for (var record : parser) {
                String investorName = record.get("투자자구분");
                String sellVolumeStr = record.get("거래량_매도");
                String buyVolumeStr = record.get("거래량_매수");
                String netVolumeStr = record.get("거래량_순매수");
                String sellValueStr = record.get("거래대금_매도");
                String buyValueStr = record.get("거래대금_매수");
                String netValueStr = record.get("거래대금_순매수");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse KRX Investor Flow CSV", e);
        }
    }
}
