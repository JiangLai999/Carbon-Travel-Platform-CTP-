package com.carbon.platform.controller;

import com.carbon.platform.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${upload.path:uploads}")
    private String uploadPathConfig;

    @Value("${upload.max-size:5242880}")
    private long maxFileSize;

    private String uploadPath;

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "webp"
    );

    @PostConstruct
    public void init() {
        File uploadDir = new File(uploadPathConfig);
        if (!uploadDir.isAbsolute()) {
            uploadDir = new File(System.getProperty("user.dir"), uploadPathConfig);
        }
        this.uploadPath = uploadDir.getAbsolutePath();
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    @PostMapping("/image")
    public ApiResponse<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("请选择要上传的文件");
        }

        if (file.getSize() > maxFileSize) {
            return ApiResponse.error("文件大小超过限制（最大5MB）");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            return ApiResponse.error("不支持的文件类型，仅支持 JPG、PNG、GIF、WEBP 格式");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "unknown.jpg";
        }

        String extension = getFileExtension(originalFilename);
        if (extension.isEmpty() || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            return ApiResponse.error("不支持的文件扩展名");
        }

        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            
            Path dirPath = Paths.get(uploadPath, dateDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            Path filePath = dirPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            String url = "/uploads/" + dateDir + "/" + newFilename;
            
            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("filename", newFilename);
            result.put("size", file.getSize());
            result.put("contentType", contentType);
            
            return ApiResponse.success(result);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.error("文件上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/images")
    public ApiResponse<?> uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return ApiResponse.error("请选择要上传的文件");
        }

        if (files.length > 9) {
            return ApiResponse.error("最多上传9张图片");
        }

        List<Map<String, Object>> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            ApiResponse<?> result = uploadImage(file);
            if (result.getCode() == 200) {
                results.add((Map<String, Object>) result.getData());
            } else {
                errors.add(result.getMessage());
            }
        }

        if (results.isEmpty()) {
            return ApiResponse.error("所有文件上传失败：" + String.join("; ", errors));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("files", results);
        response.put("total", results.size());
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }

        return ApiResponse.success(response);
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }
}
