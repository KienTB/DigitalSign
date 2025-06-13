package com.digitalsign.processor;

import com.digitalsign.exception.SignatureVerificationException;
import com.digitalsign.model.SignatureVerificationResult;
import org.apache.pdfbox.util.filetypedetector.FileType;

import java.io.InputStream;
import java.util.List;

public interface SignatureProcessor {
    SignatureVerificationResult verifySignatures(byte[] fileContent, String fileName) throws Exception;
    boolean supports(FileType fileType);
    default int getPriority() {
        return 50;
    }
    String getProcessorName();
    boolean validateFileFormat(byte[] fileContent, String fileName);
}
