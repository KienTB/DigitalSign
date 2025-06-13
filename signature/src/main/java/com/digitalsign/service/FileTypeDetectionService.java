package com.digitalsign.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileTypeDetectionService {
    String detectFileType(MultipartFile file) throws Exception;
    String detectFileType(byte[] content, String fileName) throws Exception;
    boolean isEmbeddedSignature(String fileType);
    boolean isDetachedSignature(String fileType);
    boolean isSupportedFileType(String fileType);
    String[] getSupportedFileType();

}
