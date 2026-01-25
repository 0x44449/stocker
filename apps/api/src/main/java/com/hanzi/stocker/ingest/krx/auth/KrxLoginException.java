package com.hanzi.stocker.ingest.krx.auth;

public class KrxLoginException extends RuntimeException {

    private final ErrorType errorType;

    public KrxLoginException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public KrxLoginException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public enum ErrorType {
        INVALID_CREDENTIALS,
        SESSION_NOT_ISSUED,
        UNKNOWN_RESPONSE
    }
}
