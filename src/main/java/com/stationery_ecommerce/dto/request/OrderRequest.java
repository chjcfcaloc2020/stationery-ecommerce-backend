package com.stationery_ecommerce.dto.request;

import lombok.Data;

@Data
public class OrderRequest {

    private String phoneNumber;
    private String shippingAddress;
    private String paymentMethod = "COD";
    private String voucherCode;
}
