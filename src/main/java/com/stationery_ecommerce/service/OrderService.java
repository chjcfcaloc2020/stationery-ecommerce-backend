package com.stationery_ecommerce.service;

import com.stationery_ecommerce.common.DiscountType;
import com.stationery_ecommerce.common.OrderStatus;
import com.stationery_ecommerce.common.PaymentMethod;
import com.stationery_ecommerce.dto.request.OrderRequest;
import com.stationery_ecommerce.dto.response.OrderResponse;
import com.stationery_ecommerce.entity.*;
import com.stationery_ecommerce.exception.payload.InsufficientStockException;
import com.stationery_ecommerce.exception.payload.ResourceNotFoundException;
import com.stationery_ecommerce.exception.payload.CouponException;
import com.stationery_ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;
    private final EmailService emailService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // get user
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // get all products in cart
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new ResourceNotFoundException("Cart is empty");
        }

        // handle delivery information
        String finalName = (request.getShippingName() != null && !request.getShippingName().trim().isEmpty())
                ? request.getShippingName()
                : user.getFullName();

        String finalAddress = (request.getShippingAddress() != null && !request.getShippingAddress().trim().isEmpty())
                ? request.getShippingAddress()
                : user.getLocation();

        String finalCity = (request.getShippingCity() != null && !request.getShippingCity().trim().isEmpty())
                ? request.getShippingCity()
                : user.getLocation();

        String finalPhone = (request.getShippingPhone() != null && !request.getShippingPhone().trim().isEmpty())
                ? request.getShippingPhone()
                : user.getPhone();

        if (finalAddress == null || finalPhone == null || finalName == null || finalCity == null) {
            throw new ResourceNotFoundException("Please provide your shipping information");
        }

        // Khởi tạo các biến tính toán hóa đơn
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderResponse.OrderItemDto> responseItemDtos = new ArrayList<>();

        Order order = Order.builder()
                .user(user)
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0,8).toUpperCase())
                .status(OrderStatus.valueOf("PENDING"))
                .shippingFee(request.getShippingFee())
                .shippingName(finalName)
                .shippingAddress(finalAddress)
                .shippingPhone(finalPhone)
                .shippingCity(finalCity)
                .shippingMethod(request.getShippingMethod())
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .note(request.getNote())
                .createdAt(LocalDateTime.now())
                .build();

        // Luồng xử lý từng sản phẩm - Nơi áp dụng LOCK BI QUAN
        for (CartItem cartItem: cartItems) {
            Product product = productRepository.findByIdForUpdate(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + cartItem.getProduct().getId() + " does not exist or is no longer available for sale"));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Product '" + product.getName() + "' is out of stock or there is insufficient quantity (Currently available: " + product.getStockQuantity() + ")");
            }

            // Thực hiện TRỪ KHO ĐỒNG THỜI AN TOÀN
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Chốt giá (Snapshot Price) tại thời điểm hiện tại
            BigDecimal snapshotPrice = product.getPrice();
            BigDecimal itemTotal = snapshotPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();
            orderItems.add(orderItem);

            responseItemDtos.add(OrderResponse.OrderItemDto.builder()
                    .productName(product.getName())
                    .quantity(cartItem.getQuantity())
                    .price(snapshotPrice)
                    .build());
        }

        // coupon logic
        BigDecimal discountAmount = BigDecimal.ZERO;
        Coupon appliedCoupon = null;

        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            appliedCoupon = couponRepository.findByCodeForUpdate(request.getCouponCode())
                    .orElseThrow(() -> new CouponException("Coupon is not exists"));

            // validation
            if (!appliedCoupon.isActive()) {
                throw new CouponException("Coupon was blocked");
            }

            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(appliedCoupon.getStartDate()) || now.isAfter(appliedCoupon.getEndDate())) {
                throw new CouponException("The voucher has expired or is not yet valid");
            }
            if (appliedCoupon.getMaxUses() != null && appliedCoupon.getUsedCount() >= appliedCoupon.getMaxUses()) {
                throw new CouponException("Unfortunately, this voucher has run out of uses");
            }
            if (totalAmount.compareTo(appliedCoupon.getMinOrder()) < 0) {
                throw new CouponException("The order must reach a minimum of " + appliedCoupon.getMinOrder() + "VND to apply this coupon");
            }

            // calculate reduce amount
            if (appliedCoupon.getDiscountType() == DiscountType.FIXED) {
                discountAmount = appliedCoupon.getDiscountValue();
            } else if (appliedCoupon.getDiscountType() == DiscountType.PERCENTAGE) {
                // Formula: total * (percent / 100)
                discountAmount = totalAmount.multiply(appliedCoupon.getDiscountValue().divide(BigDecimal.valueOf(100)));
            }

            // save db
            appliedCoupon.setUsedCount(appliedCoupon.getUsedCount() + 1);
            couponRepository.save(appliedCoupon);
        }

        // final calculate voucher
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        order.setTotalPrice(finalAmount);
        order.setCoupon(appliedCoupon);
        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteByUser(user);

        OrderResponse response = OrderResponse.builder()
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .totalAmount(savedOrder.getTotalPrice())
                .status(String.valueOf(savedOrder.getStatus()))
                .createdAt(savedOrder.getCreatedAt())
                .items(responseItemDtos)
                .build();

        emailService.sendOrderConfirmationEmail(user.getEmail(), response);

        return response;
    }
}
