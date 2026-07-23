package com.stationery_ecommerce.dto.request;

import com.stationery_ecommerce.common.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VoucherRequest {

    @NotBlank(message = "Voucher code is not blank")
    @Size(min = 3, max = 50, message = "Voucher code must have from 3 to 50 characters")
    private String code;

    @NotNull(message = "Voucher type is not null (FIXED or PERCENTAGE)")
    private DiscountType discountType;

    @NotNull(message = "Discount value is not null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderValue;
    private Integer usageLimit;

    @NotNull(message = "Start date is not null")
    private LocalDateTime startDate;

    @NotNull(message = "End date is not null")
    private LocalDateTime endDate;

    private boolean isActive = true;
}
