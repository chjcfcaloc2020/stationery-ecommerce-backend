package com.stationery_ecommerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {

    private Long orderId;
    private String orderNumber; // Mã đơn hàng sinh ngẫu nhiên (VD: ORD-123456)
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItemDto> items;

    @Data
    @Builder
    public static class OrderItemDto {
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}
