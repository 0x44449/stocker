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

/**
 * KRX CSV 다운로드 클라이언트.
 * download.cmd를 호출하여 CSV 파일을 다운로드.
 */
@Component
public class KrxIndexCsvDownloader {

    private static final Logger crawlLog = LoggerFactory.getLogger("CRAWL");

    private static final String DOWNLOAD_URL = "https://data.krx.co.kr/comm/fileDn/download_csv/download.cmd";
    private static final String REFERER = "https://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201020101";

    private final RestClient restClient;

    public KrxIndexCsvDownloader() {
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();
    }

    public byte[] download(KrxSession session, String otp) {
        crawlLog.info("event=KRX_INDEX_CSV_DOWNLOAD_START");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", otp);

        try {
            byte[] csvBytes = restClient.post()
                    .uri(DOWNLOAD_URL)
                    .header(HttpHeaders.COOKIE, session.toCookieValue())
                    .header(HttpHeaders.REFERER, REFERER)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(byte[].class);

            if (csvBytes == null || csvBytes.length == 0) {
                crawlLog.warn("event=KRX_INDEX_CSV_DOWNLOAD_FAILED reason=EMPTY_RESPONSE");
                throw new KrxIndexException(
                        KrxIndexException.ErrorType.CSV_DOWNLOAD_FAILED,
                        "Empty CSV response"
                );
            }

            crawlLog.info("event=KRX_INDEX_CSV_DOWNLOAD_SUCCESS bytes={}", csvBytes.length);
            return csvBytes;

        } catch (KrxIndexException e) {
            throw e;
        } catch (Exception e) {
            crawlLog.warn("event=KRX_INDEX_CSV_DOWNLOAD_FAILED reason=HTTP_ERROR");
            throw new KrxIndexException(
                    KrxIndexException.ErrorType.CSV_DOWNLOAD_FAILED,
                    "CSV download failed: " + e.getMessage(),
                    e
            );
        }
    }
}
