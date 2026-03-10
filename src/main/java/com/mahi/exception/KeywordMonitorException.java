package com.mahi.exception;

/**
 * Base exception for the application
 */
public class KeywordMonitorException extends RuntimeException {

    private String errorCode;

    public KeywordMonitorException(String message) {
        super(message);
        this.errorCode = "INTERNAL_ERROR";
    }

    public KeywordMonitorException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public KeywordMonitorException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "INTERNAL_ERROR";
    }

    public KeywordMonitorException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

