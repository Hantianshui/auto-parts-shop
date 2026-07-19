package com.autoparts.shop.controller;

import com.autoparts.shop.common.ApiResult;
import com.autoparts.shop.security.RequireRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    private final String uploadDir;

    public UploadController(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @PostMapping("/image")
    @RequireRole({"ADMIN", "MANAGER"})
    public ApiResult<Map<String, String>> image(@RequestParam MultipartFile file) throws Exception {
        String original = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String suffix = original.contains(".") ? original.substring(original.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID() + suffix;
        Path dir = Path.of(uploadDir);
        Files.createDirectories(dir);
        file.transferTo(dir.resolve(filename));
        return ApiResult.ok(Map.of("url", "/uploads/" + filename));
    }
}
