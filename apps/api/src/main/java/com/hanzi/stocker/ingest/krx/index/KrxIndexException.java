package com.hanzi.stocker.ingest.krx.index;

public class KrxIndexException extends RuntimeException {

    private final ErrorType errorType;

    public KrxIndexException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public KrxIndexException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public enum ErrorType {
        OTP_GENERATION_FAILED,
        CSV_DOWNLOAD_FAILED,
        CSV_PARSE_FAILED,
        INVALID_COLUMN_COUNT
    }
}
