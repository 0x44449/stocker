package com.hanzi.stocker.ingest.krx.index;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * KRX 지수 데이터 요청 스펙.
 */
public final class KrxIndexRequestSpec {

    private static final String REFERER = "https://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201020101";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static final String LOG_PREFIX = "KRX_INDEX";

    private KrxIndexRequestSpec() {
    }

    public static String referer() {
        return REFERER;
    }

    public static MultiValueMap<String, String> buildOtpFormData(LocalDate trdDd) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("locale", "ko_KR");
        formData.add("idxIndMidclssCd", "02");
        formData.add("trdDd", trdDd.format(DATE_FORMAT));
        formData.add("share", "1");
        formData.add("money", "1");
        formData.add("csvxls_isNo", "false");
        formData.add("name", "fileDown");
        formData.add("url", "dbms/MDC/STAT/standard/MDCSTAT00101");
        return formData;
    }
}
