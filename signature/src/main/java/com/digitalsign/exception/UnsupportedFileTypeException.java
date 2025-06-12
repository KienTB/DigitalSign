package com.digitalsign.exception;

public class UnsupportedFileTypeException extends SignatureVerificationException{
    public UnsupportedFileTypeException(String fileType) {
        super("UNSUPPORTED_FILE_TYPE", "File type not supported: " + fileType);
    }

    public UnsupportedFileTypeException(String fileType, String supportedTypes) {
        super("UNSUPPORTED_FILE_TYPE",
                String.format("File type '%s' not supported. Supported types: %s", fileType, supportedTypes));
    }
}
