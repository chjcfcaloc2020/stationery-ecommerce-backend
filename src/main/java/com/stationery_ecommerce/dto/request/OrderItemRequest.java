package com.stationery_ecommerce.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {

    @NotNull(message = "ID product is not null")
    private Long productId;

    @NotNull(message = "Quantity is not null")
    @Min(value = 1, message = "Quantity must be at leatest 1")
    private Integer quantity;
}
