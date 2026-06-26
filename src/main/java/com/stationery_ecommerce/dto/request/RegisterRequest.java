package com.stationery_ecommerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "FullName is not blank")
    private String fullName;

    @NotBlank(message = "Email is not blank")
    @Email(message = "Format is invalid")
    private String email;

    @NotBlank(message = "Password is not blank")
    @Size(min = 6, message = "Password must be at least 6 char")
    private String password;
}
