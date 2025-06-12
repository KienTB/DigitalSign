package com.digitalsign.exception;

public class CertificateException extends SignatureVerificationException{
    public CertificateException(String message) {
        super("CERTIFICATE_ERROR", message);
    }

    public CertificateException(String message, Throwable cause) {
        super("CERTIFICATE_ERROR", message, cause);
    }
}
