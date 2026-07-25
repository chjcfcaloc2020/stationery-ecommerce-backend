package com.stationery_ecommerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/jpg"
    );

    public String uploadImage(MultipartFile file, String folderName) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is not empty!");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Format file is invalid! Accept to JPG, PNG, GIF, WEBP.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Image size maximum is 5MB!");
        }

        String fileName = UUID.randomUUID().toString();

        Map<String, Object> params = ObjectUtils.asMap(
                "folder", folderName,          // Thư mục lưu trữ trên Cloudinary
                "public_id", fileName,         // Tên file trên Cloud
                "resource_type", "image"       // Loại tài nguyên
        );

        // Thực hiện upload và lấy kết quả trả về
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

        // Trả về đường dẫn HTTPS bảo mật của ảnh
        return uploadResult.get("secure_url").toString();
    }
}
