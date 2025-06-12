package com.digitalsign.exception;

public class InvalidSignatureException extends SignatureVerificationException{
    public InvalidSignatureException(String message) {
        super("INVALID_SIGNATURE", message);
    }

    public InvalidSignatureException(String message, Throwable cause) {
        super("INVALID_SIGNATURE", message, cause);
    }

    public InvalidSignatureException(String reason, String details) {
        super("INVALID_SIGNATURE", String.format("Invalid signature: %s. Details: %s", reason, details));
    }
}
