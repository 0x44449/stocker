package com.hanzi.stocker.ingest.krx.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * KRX 로그인 응답 JSON DTO.
 * auth 패키지 내부에서만 사용.
 */
final class KrxLoginResponse {

    @JsonProperty("previousMemberYn")
    private boolean previousMemberYn;

    @JsonProperty("MDC_MBR_TP_CD")
    private String mdcMbrTpCd;

    @JsonProperty("MBR_NO")
    private String mbrNo;

    @JsonProperty("_error_code")
    private String errorCode;

    @JsonProperty("_error_message")
    private String errorMessage;

    public boolean isPreviousMemberYn() {
        return previousMemberYn;
    }

    public String getMdcMbrTpCd() {
        return mdcMbrTpCd;
    }

    public String getMbrNo() {
        return mbrNo;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isSuccess() {
        return "CD001".equals(errorCode);
    }
}
