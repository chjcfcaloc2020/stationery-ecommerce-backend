package com.stationery_ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WishlistRequest {

    @NotNull(message = "Product ID is not null")
    private Long productId;
}
