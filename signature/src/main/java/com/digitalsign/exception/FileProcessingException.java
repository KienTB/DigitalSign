package com.digitalsign.exception;

public class FileProcessingException extends SignatureVerificationException{
    public FileProcessingException(String message) {
        super("FILE_PROCESSING_ERROR", message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super("FILE_PROCESSING_ERROR", message, cause);
    }
}
