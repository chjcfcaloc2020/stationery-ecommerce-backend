package com.stationery_ecommerce.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {

    @NotNull(message = "Product ID is not null")
    private Long productId;

    @NotNull(message = "Product is not null")
    @Min(value = 1, message = "The minimum quantity added to the cart is 1")
    private Integer quantity;
}
