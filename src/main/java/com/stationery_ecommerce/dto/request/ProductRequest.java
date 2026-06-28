package com.stationery_ecommerce.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "Product's name is not blank")
    private String name;

    private String description;

    @NotNull(message = "Product's price is not null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Product's stock quantity is not null")
    @Min(value = 0, message = "Số lượng kho không được âm")
    private Integer stockQuantity;

    @NotBlank(message = "SKU code is not blank")
    private String sku;

    @NotNull(message = "Category ID is not null")
    private Long categoryId;
}
