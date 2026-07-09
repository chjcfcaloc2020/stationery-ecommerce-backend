package com.stationery_ecommerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private List<CartItemDto> items;
    private BigDecimal totalCartAmount;

    @Data
    @Builder
    public static class CartItemDto {
        private Long cartItemId;
        private Long productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subTotal; // Giá * Số lượng của riêng sản phẩm này
    }
}
