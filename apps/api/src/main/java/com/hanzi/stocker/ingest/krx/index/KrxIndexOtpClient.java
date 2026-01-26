package com.hanzi.stocker.ingest.krx.index;

import com.hanzi.stocker.ingest.krx.auth.KrxSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * KRX OTP 생성 클라이언트.
 * generate.cmd를 호출하여 CSV 다운로드용 OTP code를 획득.
 */
@Component
public class KrxIndexOtpClient {

    private static final Logger crawlLog = LoggerFactory.getLogger("CRAWL");

    private static final String GENERATE_URL = "https://data.krx.co.kr/comm/fileDn/GenerateOTP/generate.cmd";
    private static final String REFERER = "https://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201020101";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RestClient restClient;

    public KrxIndexOtpClient() {
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();
    }

    public String generateOtp(KrxSession session, LocalDate trdDd) {
        String trdDdStr = trdDd.format(DATE_FORMAT);
        crawlLog.info("event=KRX_INDEX_OTP_START trdDd={}", trdDdStr);

        MultiValueMap<String, String> formData = buildFormData(trdDdStr);

        try {
            String otp = restClient.post()
                    .uri(GENERATE_URL)
                    .header(HttpHeaders.COOKIE, session.toCookieValue())
                    .header(HttpHeaders.REFERER, REFERER)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(String.class);

            if (otp == null || otp.isBlank()) {
                crawlLog.warn("event=KRX_INDEX_OTP_FAILED trdDd={} reason=EMPTY_RESPONSE", trdDdStr);
                throw new KrxIndexException(
                        KrxIndexException.ErrorType.OTP_GENERATION_FAILED,
                        "Empty OTP response"
                );
            }

            crawlLog.info("event=KRX_INDEX_OTP_SUCCESS trdDd={}", trdDdStr);
            return otp.trim();

        } catch (KrxIndexException e) {
            throw e;
        } catch (Exception e) {
            crawlLog.warn("event=KRX_INDEX_OTP_FAILED trdDd={} reason=HTTP_ERROR", trdDdStr);
            throw new KrxIndexException(
                    KrxIndexException.ErrorType.OTP_GENERATION_FAILED,
                    "OTP generation failed: " + e.getMessage(),
                    e
            );
        }
    }

    private MultiValueMap<String, String> buildFormData(String trdDd) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("locale", "ko_KR");
        formData.add("idxIndMidclssCd", "02");
        formData.add("trdDd", trdDd);
        formData.add("share", "1");
        formData.add("money", "1");
        formData.add("csvxls_isNo", "false");
        formData.add("name", "fileDown");
        formData.add("url", "dbms/MDC/STAT/standard/MDCSTAT00101");
        return formData;
    }
}
