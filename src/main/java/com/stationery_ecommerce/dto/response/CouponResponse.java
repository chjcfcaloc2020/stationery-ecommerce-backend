package com.stationery_ecommerce.dto.response;

import com.stationery_ecommerce.common.DiscountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CouponResponse {
    private Long id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
//    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrder;
    private Integer maxUses;
    private Integer usedCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;
}
