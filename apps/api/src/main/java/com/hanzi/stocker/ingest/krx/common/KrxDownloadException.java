package com.hanzi.stocker.ingest.krx.common;

public class KrxDownloadException extends RuntimeException {

    private final ErrorType errorType;

    public KrxDownloadException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public KrxDownloadException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public enum ErrorType {
        OTP_GENERATION_FAILED,
        CSV_DOWNLOAD_FAILED
    }
}
