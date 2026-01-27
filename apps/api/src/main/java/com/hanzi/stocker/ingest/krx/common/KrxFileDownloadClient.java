package com.hanzi.stocker.ingest.krx.common;

import com.hanzi.stocker.ingest.krx.auth.KrxSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * KRX 파일 다운로드 공통 클라이언트.
 * OTP 생성 및 CSV 다운로드를 담당.
 */
@Component
public class KrxFileDownloadClient {

    private static final Logger crawlLog = LoggerFactory.getLogger("CRAWL");

    private static final String GENERATE_URL = "https://data.krx.co.kr/comm/fileDn/GenerateOTP/generate.cmd";
    private static final String DOWNLOAD_URL = "https://data.krx.co.kr/comm/fileDn/download_csv/download.cmd";

    private final RestClient restClient;

    public KrxFileDownloadClient() {
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();
    }

    /**
     * OTP 생성.
     */
    public String generateOtp(KrxSession session, String referer, MultiValueMap<String, String> formData, String logPrefix) {
        crawlLog.info("event={}_OTP_START", logPrefix);

        try {
            String otp = restClient.post()
                    .uri(GENERATE_URL)
                    .header(HttpHeaders.COOKIE, session.toCookieValue())
                    .header(HttpHeaders.REFERER, referer)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(String.class);

            if (otp == null || otp.isBlank()) {
                crawlLog.warn("event={}_OTP_FAILED reason=EMPTY_RESPONSE", logPrefix);
                throw new KrxDownloadException(
                        KrxDownloadException.ErrorType.OTP_GENERATION_FAILED,
                        "Empty OTP response"
                );
            }

            crawlLog.info("event={}_OTP_SUCCESS", logPrefix);
            return otp.trim();

        } catch (KrxDownloadException e) {
            throw e;
        } catch (Exception e) {
            crawlLog.warn("event={}_OTP_FAILED reason=HTTP_ERROR", logPrefix);
            throw new KrxDownloadException(
                    KrxDownloadException.ErrorType.OTP_GENERATION_FAILED,
                    "OTP generation failed: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * CSV 다운로드.
     */
    public byte[] downloadCsv(KrxSession session, String referer, String otp, String logPrefix) {
        crawlLog.info("event={}_CSV_DOWNLOAD_START", logPrefix);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", otp);

        try {
            byte[] csvBytes = restClient.post()
                    .uri(DOWNLOAD_URL)
                    .header(HttpHeaders.COOKIE, session.toCookieValue())
                    .header(HttpHeaders.REFERER, referer)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(byte[].class);

            if (csvBytes == null || csvBytes.length == 0) {
                crawlLog.warn("event={}_CSV_DOWNLOAD_FAILED reason=EMPTY_RESPONSE", logPrefix);
                throw new KrxDownloadException(
                        KrxDownloadException.ErrorType.CSV_DOWNLOAD_FAILED,
                        "Empty CSV response"
                );
            }

            crawlLog.info("event={}_CSV_DOWNLOAD_SUCCESS bytes={}", logPrefix, csvBytes.length);
            return csvBytes;

        } catch (KrxDownloadException e) {
            throw e;
        } catch (Exception e) {
            crawlLog.warn("event={}_CSV_DOWNLOAD_FAILED reason=HTTP_ERROR", logPrefix);
            throw new KrxDownloadException(
                    KrxDownloadException.ErrorType.CSV_DOWNLOAD_FAILED,
                    "CSV download failed: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * OTP 생성 + CSV 다운로드를 한 번에 수행.
     */
    public byte[] download(KrxSession session, String referer, MultiValueMap<String, String> otpFormData, String logPrefix) {
        String otp = generateOtp(session, referer, otpFormData, logPrefix);
        return downloadCsv(session, referer, otp, logPrefix);
    }
}
