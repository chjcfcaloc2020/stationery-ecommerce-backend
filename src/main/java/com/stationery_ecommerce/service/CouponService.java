package com.stationery_ecommerce.service;

import com.stationery_ecommerce.dto.request.CouponRequest;
import com.stationery_ecommerce.dto.response.CouponResponse;
import com.stationery_ecommerce.entity.Coupon;
import com.stationery_ecommerce.exception.payload.ResourceNotFoundException;
import com.stationery_ecommerce.exception.payload.CouponException;
import com.stationery_ecommerce.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new CouponException("Coupon code '" + request.getCode() + "' existed in system!");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new CouponException("End date must after start date!");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
//                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrder(request.getMinOrder())
                .maxUses(request.getMaxUses())
                .usedCount(0)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.isActive())
                .build();

        return mapToResponse(couponRepository.save(coupon));
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> getAllVouchers() {
        return couponRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CouponResponse getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with ID: " + id));
        return mapToResponse(coupon);
    }

    @Transactional
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with ID: " + id));

        if (!coupon.getCode().equalsIgnoreCase(request.getCode()) && couponRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new CouponException("Coupon code '" + request.getCode() + "' existed!");
        }

        coupon.setCode(request.getCode().toUpperCase());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
//        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setMinOrder(request.getMinOrder());
        coupon.setMaxUses(request.getMaxUses());
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        coupon.setActive(request.isActive());

        return mapToResponse(couponRepository.save(coupon));
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with ID: " + id));

        if (coupon.getUsedCount() > 0) {
            coupon.setActive(false);
            couponRepository.save(coupon);
        } else {
            couponRepository.delete(coupon);
        }
    }

    private CouponResponse mapToResponse(Coupon c) {
        return CouponResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .discountType(c.getDiscountType())
                .discountValue(c.getDiscountValue())
//                .maxDiscountAmount(v.getMaxDiscountAmount())
                .minOrder(c.getMinOrder())
                .maxUses(c.getMaxUses())
                .usedCount(c.getUsedCount())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .isActive(c.isActive())
                .build();
    }
}
