package com.stationery_ecommerce.service;

import com.stationery_ecommerce.common.OrderStatus;
import com.stationery_ecommerce.common.PaymentMethod;
import com.stationery_ecommerce.dto.request.OrderItemRequest;
import com.stationery_ecommerce.dto.request.OrderRequest;
import com.stationery_ecommerce.dto.response.OrderResponse;
import com.stationery_ecommerce.entity.*;
import com.stationery_ecommerce.exception.payload.InsufficientStockException;
import com.stationery_ecommerce.exception.payload.ResourceNotFoundException;
import com.stationery_ecommerce.repository.CartItemRepository;
import com.stationery_ecommerce.repository.OrderRepository;
import com.stationery_ecommerce.repository.ProductRepository;
import com.stationery_ecommerce.repository.UserRepository;
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
        String finalAddress = (request.getShippingAddress() != null && !request.getShippingAddress().trim().isEmpty())
                ? request.getShippingAddress()
                : user.getLocation();

        String finalPhone = (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty())
                ? request.getPhoneNumber()
                : user.getPhone();

        if (finalAddress == null || finalPhone == null) {
            throw new ResourceNotFoundException("Please provide your shipping address and phone number");
        }

        // Khởi tạo các biến tính toán hóa đơn
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderResponse.OrderItemDto> responseItemDtos = new ArrayList<>();

        Order order = Order.builder()
                .user(user)
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0,8).toUpperCase())
                .status(OrderStatus.valueOf("PENDING"))
                .shippingAddress(finalAddress)
                .phoneNumber(finalPhone)
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
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

        order.setTotalPrice(totalAmount);
        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteByUser(user);

        return OrderResponse.builder()
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .totalAmount(savedOrder.getTotalPrice())
                .status(String.valueOf(savedOrder.getStatus()))
                .createdAt(savedOrder.getCreatedAt())
                .items(responseItemDtos)
                .build();
    }
}
