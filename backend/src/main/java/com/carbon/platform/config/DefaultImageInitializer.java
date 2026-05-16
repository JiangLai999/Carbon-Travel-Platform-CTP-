package com.carbon.platform.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class DefaultImageInitializer {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    private static final Map<String, String> DEFAULT_IMAGES = new LinkedHashMap<>();
    
    static {
        DEFAULT_IMAGES.put("avatars/avatar_1.jpg", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100");
        DEFAULT_IMAGES.put("avatars/avatar_2.jpg", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100");
        DEFAULT_IMAGES.put("avatars/avatar_3.jpg", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100");
        DEFAULT_IMAGES.put("avatars/avatar_4.jpg", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100");
        DEFAULT_IMAGES.put("avatars/avatar_5.jpg", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100");
        
        DEFAULT_IMAGES.put("products/bike_folding.jpg", "https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=400");
        DEFAULT_IMAGES.put("products/bike_mountain.jpg", "https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=400");
        DEFAULT_IMAGES.put("products/helmet.jpg", "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400");
        DEFAULT_IMAGES.put("products/gloves.jpg", "https://images.unsplash.com/photo-1606902965551-dce093cda6e7?w=400");
        DEFAULT_IMAGES.put("products/backpack.jpg", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400");
        DEFAULT_IMAGES.put("products/raincoat.jpg", "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=400");
        
        DEFAULT_IMAGES.put("activities/cycling_event.jpg", "https://images.unsplash.com/photo-1541625602330-2277a4c46182?w=400");
        DEFAULT_IMAGES.put("activities/knowledge_quiz.jpg", "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=400");
        DEFAULT_IMAGES.put("activities/spring_travel.jpg", "https://images.unsplash.com/photo-1514565131-fce0801e5785?w=400");
        DEFAULT_IMAGES.put("activities/lantern_festival.jpg", "https://images.unsplash.com/photo-1514539079130-25950c84af65?w=400");
        DEFAULT_IMAGES.put("activities/earth_hour.jpg", "https://images.unsplash.com/photo-1473448912268-2022ce9509d8?w=400");
    }

    @PostConstruct
    public void initDefaultImages() {
        File uploadDir = new File(uploadPath);
        if (!uploadDir.isAbsolute()) {
            uploadDir = new File(System.getProperty("user.dir"), uploadPath);
        }
        
        log.info("检查默认图片...");
        int downloaded = 0;
        int skipped = 0;
        
        for (Map.Entry<String, String> entry : DEFAULT_IMAGES.entrySet()) {
            String localPath = entry.getKey();
            String remoteUrl = entry.getValue();
            
            File file = new File(uploadDir, localPath);
            
            if (file.exists() && file.length() > 0) {
                skipped++;
                continue;
            }
            
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try {
                downloadFile(remoteUrl, file);
                downloaded++;
                log.info("下载图片: {}", localPath);
            } catch (Exception e) {
                log.warn("下载图片失败 {}: {}", localPath, e.getMessage());
            }
        }
        
        if (downloaded > 0 || skipped > 0) {
            log.info("默认图片检查完成: 下载={}, 已存在={}", downloaded, skipped);
        }
    }
    
    private void downloadFile(String urlStr, File destFile) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || 
            responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
            responseCode == 302) {
            String newUrl = conn.getHeaderField("Location");
            conn.disconnect();
            downloadFile(newUrl, destFile);
            return;
        }
        
        if (responseCode != 200) {
            throw new IOException("HTTP " + responseCode);
        }
        
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        conn.disconnect();
    }
}
