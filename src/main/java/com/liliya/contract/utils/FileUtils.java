package com.liliya.contract.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class FileUtils {
    @Value("${contract.dirPath}")
    private String dirPath;

    // 新增文件
    public String add(byte[] fileContent){
        // 确保路径可用
        File filePath = new File(dirPath);
        if(!filePath.exists()){
            if (!filePath.mkdirs()){
                return null;
            }
        }
        // 更改文件名为随机UUID
        String fileName = UUID.randomUUID().toString();
        // 完整路径
        String dest = dirPath + fileName;
        // 尝试新增文件
        try(FileOutputStream fileOutputStream = new FileOutputStream(dest)) {
            fileOutputStream.write(fileContent);
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return dest;
    }

    // 新增文件
    public String add(MultipartFile file){
        // 确保路径可用
        File filePath = new File(dirPath);
        if(!filePath.exists()){
            if (!filePath.mkdirs()){
                return null;
            }
        }
        // 更改文件名为随机UUID
        String fileName = UUID.randomUUID().toString();
        // 完整路径
        String dest = dirPath + fileName;
        // 尝试新增文件
        try {
            file.transferTo(new File(dest));
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return dest;
    }

    // 删除文件
    public boolean delete(String url){
        return new File(url).delete();
    }

    // 批量关闭文件流
    public static void close(AutoCloseable... t) {
        for (AutoCloseable closeable : t) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getFilename(HttpServletRequest request, String filename) {
        String[] IEBrowserKeyWords = {"MSIE", "Trident", "Edge"};
        String userAgent = request.getHeader("User-Agent");
        for (String keyWord : IEBrowserKeyWords) {
            if (userAgent.contains(keyWord)) {
                return URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+"," ");
            }}
        return new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
    }
}
