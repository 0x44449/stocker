package com.hanzi.stocker.ingest.krx.investor;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * KRX 투자자별 거래실적 요청 스펙.
 */
public final class KrxInvestorFlowRequestSpec {

    private static final String REFERER = "https://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201020101";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static final String LOG_PREFIX = "KRX_INVESTOR";

    private KrxInvestorFlowRequestSpec() {
    }

    public static String referer() {
        return REFERER;
    }

    public static MultiValueMap<String, String> buildOtpFormData(LocalDate trdDd, String market) {
        String trdDdStr = trdDd.format(DATE_FORMAT);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("locale", "ko_KR");
        formData.add("inqTpCd", "1");
        formData.add("trdVolVal", "2");
        formData.add("askBid", "3");
        formData.add("mktId", market);
        formData.add("etf", "EF");
        formData.add("etn", "EN");
        formData.add("elw", "EW");
        formData.add("strtDd", trdDdStr);
        formData.add("endDd", trdDdStr);
        formData.add("share", "1");
        formData.add("money", "1");
        formData.add("csvxls_isNo", "false");
        formData.add("name", "fileDown");
        formData.add("url", "dbms/MDC/STAT/standard/MDCSTAT02201");
        return formData;
    }
}
