package com.digitalsign.util;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class FileUtils {
    private final Tika tika = new Tika();

    public String detectFileType(byte[] content){
        return tika.detect(content);
    }

    public String detectFileType(MultipartFile file) throws IOException{
        return tika.detect(file.getInputStream(), file.getOriginalFilename());
    }

    public String getFileExtension(String filename){
        if (filename == null || filename.lastIndexOf('.') == 1){
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')+1).toLowerCase();
    }

    public boolean isSupportedFileType(String mimeType){
        if (mimeType == null) return false;

        return mimeType.equals("application/pdf") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
                mimeType.equals("text/xml") ||
                mimeType.equals("application/xml") ||
                mimeType.equals("application/pkcs7-signature") ||
                mimeType.equals("application/x-pkcs7-signature");
    }

    public byte[] readAllBytes(MultipartFile file) throws IOException{
        try(InputStream inputStream = file.getInputStream()){
            return inputStream.readAllBytes();
        }
    }

    public String calculateFileHash(byte[] content, String algoristhm) throws NoSuchAlgorithmException{
        MessageDigest digest = MessageDigest.getInstance(algoristhm);
        byte[] hash = digest.digest(content);
        return HexFormat.of().formatHex(hash);
    }

    public String calculateSHA25Hash(byte[] content) throws NoSuchAlgorithmException{
        return calculateFileHash(content, "SHA-256");
    }

    public Path saveTempFile(byte[] content, String prefix, String suffix) throws IOException{
        Path tempFile = Files.createTempFile(prefix, suffix);
        Files.write(tempFile, content);
        return tempFile;
    }

    public void deleteTempFile(Path tempFile){
        try{
            if(tempFile != null && Files.exists(tempFile)){
                Files.delete(tempFile);
            }
        }catch (IOException e){
            System.err.println("Warning: Could not delete temp file: " + tempFile);
        }
    }

    public boolean isValidFileSize(long size, long maxSize){
        return size > 0 && size <= maxSize;
    }

    public String formatFileSize(long size){
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
}
