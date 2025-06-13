package com.digitalsign.service.impl;

import com.digitalsign.service.FileTypeDetectionService;
import com.digitalsign.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Set;

@Service
public class FileTypeDetectionServiceImpl implements FileTypeDetectionService {
    @Autowired
    private FileUtils fileUtils;

    private static final Set<String> EMBEDDED_SIGNATURE_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/xml",
            "application/xml"
    );

    private static final Set<String> DETACHED_SIGNATURE_TYPES = Set.of(
            "application/pkcs7-signature",
            "application/x-pkcs7-signature",
            "application/pkcs7-mime",
            "text/plain"
    );

    @Override
    public String detectFileType(MultipartFile file) throws Exception{
        try{
            String mimeType = fileUtils.detectFileType(file);

            if(mimeType == null || mimeType.equals("application/octet-stream")){
                String extension = fileUtils.getFileExtension(file.getOriginalFilename());
                mimeType = inferMimeTypeFromExtension(extension);
            }
            return mimeType;
        }catch (IOException e){
            throw new Exception("ERROR detecting file type", e);
        }
    }

    @Override
    public String detectFileType(byte[] content, String fileName) throws Exception{
        String mimeType = fileUtils.detectFileType(content);

        if(mimeType == null || mimeType.equals("application/octet-stream")){
            String extension = fileUtils.getFileExtension(fileName);
            mimeType = inferMimeTypeFromExtension(extension);
        }
        return  mimeType;
    }

    @Override
    public boolean isEmbeddedSignature(String fileType){
        return EMBEDDED_SIGNATURE_TYPES.contains(fileType);
    }

    @Override
    public boolean isDetachedSignature(String fileType){
        return EMBEDDED_SIGNATURE_TYPES.contains(fileType);
    }

    @Override
    public boolean isSupportedFileType(String fileType){
        return isEmbeddedSignature(fileType) || isDetachedSignature(fileType);
    }

    @Override
    public String[] getSupportedFileType(){
        return Arrays.stream(EMBEDDED_SIGNATURE_TYPES.toArray(new String[0]))
                .collect(java.util.stream.Collectors.toList())
                .toArray(String[]::new);
    }

    private String inferMimeTypeFromExtension(String extension){
        if (extension == null) return "application/octet-stream";

        switch (extension.toLowerCase()){
            case "pdf":
                return "application/pdf";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "xml":
                return "application/xml";
            case "sig":
            case "p7s":
                return "application/pkcs7-signature";
            case "p7m":
                return "application/pkcs7-mime";
            default:
                return "application/octet-stream";
        }
    }

    public String getFileTypeDescription(String mimeType){
        switch (mimeType){
            case "application/pdf":
                return "PDF Document";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return "Microsoft Word Document";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return "Microsoft Excel Spreadsheet";
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return "Microsoft PowerPoint Presentation";
            case "text/xml":
            case "application/xml":
                return "XML Document";
            case "application/pkcs7-signature":
            case "application/x-pkcs7-signature":
                return "PKCS#7 Signature";
            case "text/plain":
                return "Text File";
            default:
                return "Unknown File Type";
        }
    }

    public boolean isExtensionMimeTypeMatch(String fileName, String mimeType){
        String extension = fileUtils.getFileExtension(fileName);
        String expectedMimeType = inferMimeTypeFromExtension(extension);
        return expectedMimeType.equals(mimeType);
    }

    public void validateFileForProcessing(MultipartFile file) throws Exception{
        if(file == null || file.isEmpty()){
            throw new Exception("File is empty or null");
        }

        if(file.getSize() > 50 * 1024 * 1024){
            throw new Exception("File size exceeds maximun limit (50MB)");
        }

        String mimeType = detectFileType(file);
        if(!isSupportedFileType(mimeType)){
            throw new Exception("Unsupported file type: " + mimeType);
        }
    }
}
