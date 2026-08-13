package com.stationery_ecommerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WishlistResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String slug;
    private BigDecimal price;
    private Double rating;
    private Integer reviewCount;
    private String imageUrl;
    private String brand;
    private boolean isNew;
    private boolean isBestSeller;
    private boolean isFeatured;
    private boolean isOnSale;
}
