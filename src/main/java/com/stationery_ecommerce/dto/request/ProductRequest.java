package com.stationery_ecommerce.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {

    @NotBlank(message = "Product's name is not blank")
    private String name;

    private String description;

    @NotNull(message = "Product's price is not null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Product's price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Original Product's price is not null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Original Product's price must be greater than 0")
    private BigDecimal originalPrice;

    @NotNull(message = "Product's stock quantity is not null")
    @Min(value = 0, message = "Số lượng kho không được âm")
    private Integer stockQuantity;

    private String imageUrl;
    private List<String> images;
    private List<String> tags;
    private boolean isNew;
    private boolean isBestSeller;
    private boolean isFeatured;
    private boolean isOnSale;

    @NotBlank(message = "Product's brand is not blank")
    private String brand;

    @NotNull(message = "Category ID is not null")
    private Long categoryId;
}
