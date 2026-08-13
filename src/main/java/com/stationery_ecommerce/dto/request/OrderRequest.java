package com.stationery_ecommerce.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {

    private BigDecimal shippingFee;
    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private String shippingCity;
    private String shippingMethod;
    private String paymentMethod = "COD";
    private String note;
    private String couponCode;
}
