package com.digitalsign.util;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
}
