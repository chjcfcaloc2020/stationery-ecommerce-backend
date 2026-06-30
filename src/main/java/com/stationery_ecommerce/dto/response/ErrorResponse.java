package com.stationery_ecommerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ErrorResponse {
    private int status;              // Mã HTTP Status Code (VD: 400, 404, 403)
    private String error;            // Loại lỗi ngắn gọn (VD: "BAD_REQUEST", "NOT_FOUND")
    private String message;          // Thông báo lỗi chi tiết bằng Tiếng Việt thân thiện với UI
    private LocalDateTime timestamp; // Thời gian xảy ra lỗi để phục vụ việc tra cứu Log
    private Map<String, String> errors;
}
