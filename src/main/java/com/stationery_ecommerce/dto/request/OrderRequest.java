package com.stationery_ecommerce.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotEmpty(message = "Items must be at leatest 1")
    private List<OrderItemRequest> items;

    private String shippingAddress;
    private String paymentMethod;
}
