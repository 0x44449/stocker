package com.hanzi.stocker.ingest.krx.common;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class KrxFileClient {

    private static final String GENERATE_URL = "https://data.krx.co.kr/comm/fileDn/GenerateOTP/generate.cmd";
    private static final String DOWNLOAD_URL = "https://data.krx.co.kr/comm/fileDn/download_csv/download.cmd";

    private final RestClient restClient;

    public KrxFileClient() {
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();
    }

    private String generateOtp(KrxAuth.LoginSession session, String referer, MultiValueMap<String, String> formData) {
        String otp = restClient.post()
                .uri(GENERATE_URL)
                .header(HttpHeaders.COOKIE, session.toCookieValue())
                .header(HttpHeaders.REFERER, referer)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(String.class);

        if (otp == null || otp.isBlank()) {
            throw new IllegalStateException("Failed to generate OTP: empty response");
        }

        return otp.trim();
    }

    private byte[] downloadFile(KrxAuth.LoginSession session, String referer, String otp) {
        var formData = new LinkedMultiValueMap<>();
        formData.add("code", otp);

        return restClient.post()
                .uri(DOWNLOAD_URL)
                .header(HttpHeaders.COOKIE, session.toCookieValue())
                .header(HttpHeaders.REFERER, referer)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(byte[].class);
    }

    public byte[] download(KrxAuth.LoginSession session, String referer, MultiValueMap<String, String> formData) {
        String otp = generateOtp(session, referer, formData);
        return downloadFile(session, referer, otp);
    }
}
