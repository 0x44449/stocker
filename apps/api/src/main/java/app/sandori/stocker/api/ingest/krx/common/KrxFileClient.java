package app.sandori.stocker.api.ingest.krx.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class KrxFileClient {

    private static final Logger log = LoggerFactory.getLogger(KrxFileClient.class);

    private static final String GENERATE_URL = "https://data.krx.co.kr/comm/fileDn/GenerateOTP/generate.cmd";
    private static final String DOWNLOAD_URL = "https://data.krx.co.kr/comm/fileDn/download_csv/download.cmd";

    private final RestClient restClient;

    public KrxFileClient() {
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();
    }

    private String generateOtp(KrxSession session, String referer, MultiValueMap<String, String> formData) {
        log.info("OTP 생성 시도 - cookie: {}, referer: {}", session.toCookieValue(), referer);
        log.debug("OTP formData: {}", formData);

        String otp = restClient.post()
                .uri(GENERATE_URL)
                .header(HttpHeaders.COOKIE, session.toCookieValue())
                .header(HttpHeaders.REFERER, referer)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(String.class);

        if (otp == null || otp.isBlank()) {
            log.error("OTP 생성 실패 - 빈 응답");
            throw new IllegalStateException("Failed to generate OTP: empty response");
        }

        log.info("OTP 생성 성공 - otp: {}", otp.trim());
        return otp.trim();
    }

    private byte[] downloadFile(KrxSession session, String referer, String otp) {
        log.info("파일 다운로드 시도 - otp: {}", otp);

        var formData = new LinkedMultiValueMap<String, String>();
        formData.add("code", otp);

        byte[] result = restClient.post()
                .uri(DOWNLOAD_URL)
                .header(HttpHeaders.COOKIE, session.toCookieValue())
                .header(HttpHeaders.REFERER, referer)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(byte[].class);

        if (result == null) {
            log.error("파일 다운로드 실패 - null 응답");
        } else {
            log.info("파일 다운로드 성공 - {} bytes", result.length);
        }

        return result;
    }

    public byte[] download(KrxSession session, String referer, MultiValueMap<String, String> formData) {
        log.info("KRX 파일 다운로드 시작");
        String otp = generateOtp(session, referer, formData);
        return downloadFile(session, referer, otp);
    }
}
