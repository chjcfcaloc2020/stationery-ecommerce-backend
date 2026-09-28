package com.stationery_ecommerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String categoryName;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stockQuantity;
    private Double rating;
    private Integer reviewCount;
    private String imageUrl;
    private List<String> images;
    private List<String> tags;
    private boolean isAvailable;
    private boolean isNew;
    private boolean isBestSeller;
    private boolean isFeatured;
    private boolean isOnSale;
    private List<String> colors;
    private String brand;
}
