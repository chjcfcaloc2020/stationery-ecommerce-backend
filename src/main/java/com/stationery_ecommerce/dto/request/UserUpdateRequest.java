package com.stationery_ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    private String phone;
    private String location;
    private String role;     // ROLE_USER hoặc ROLE_ADMIN (Chỉ Admin mới có quyền cập nhật trường này)
    private Boolean isActive;
}
