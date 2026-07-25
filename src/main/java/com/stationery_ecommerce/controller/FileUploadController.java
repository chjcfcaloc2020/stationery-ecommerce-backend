package com.stationery_ecommerce.controller;

import com.stationery_ecommerce.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    // 1. Upload ảnh sản phẩm (Chỉ Admin mới có quyền upload)
    @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> uploadProductImage(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        String imageUrl = fileUploadService.uploadImage(file, "stationery_products");

        Map<String, String> response = new HashMap<>();
        response.put("url", imageUrl);
        return ResponseEntity.ok(response);
    }

    // 2. Upload avatar người dùng (Mọi tài khoản đã đăng nhập đều được upload)
    @PostMapping("/avatars")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadUserAvatar(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        String avatarUrl = fileUploadService.uploadImage(file, "user_avatars");

        Map<String, String> response = new HashMap<>();
        response.put("url", avatarUrl);
        return ResponseEntity.ok(response);
    }
}
