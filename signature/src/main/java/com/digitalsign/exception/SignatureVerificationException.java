package com.digitalsign.exception;

public class SignatureVerificationException extends Exception{
    private String errorCode;

    public SignatureVerificationException(String message) {
        super(message);
    }

    public SignatureVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SignatureVerificationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SignatureVerificationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
