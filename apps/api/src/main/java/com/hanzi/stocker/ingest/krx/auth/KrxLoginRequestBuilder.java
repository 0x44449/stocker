package com.hanzi.stocker.ingest.krx.auth;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * KRX 로그인 요청 body를 생성.
 * x-www-form-urlencoded 형식.
 */
final class KrxLoginRequestBuilder {

    private static final String FIELD_MBR_NM = "mbrNm";
    private static final String FIELD_TEL_NO = "telNo";
    private static final String FIELD_DI = "di";
    private static final String FIELD_CERT_TYPE = "certType";
    private static final String FIELD_MBR_ID = "mbrId";
    private static final String FIELD_PW = "pw";
    private static final String FIELD_SKIP_DUP = "skipDup";

    private static final String SKIP_DUP_VALUE = "Y";

    private String mbrId;
    private String pw;

    public KrxLoginRequestBuilder mbrId(String mbrId) {
        this.mbrId = mbrId;
        return this;
    }

    public KrxLoginRequestBuilder pw(String pw) {
        this.pw = pw;
        return this;
    }

    public MultiValueMap<String, String> build() {
        if (mbrId == null || mbrId.isBlank()) {
            throw new IllegalStateException("mbrId must be set");
        }
        if (pw == null || pw.isBlank()) {
            throw new IllegalStateException("pw must be set");
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(FIELD_MBR_NM, "");
        formData.add(FIELD_TEL_NO, "");
        formData.add(FIELD_DI, "");
        formData.add(FIELD_CERT_TYPE, "");
        formData.add(FIELD_MBR_ID, mbrId);
        formData.add(FIELD_PW, pw);
        formData.add(FIELD_SKIP_DUP, SKIP_DUP_VALUE);

        return formData;
    }
}
