package com.stationery_ecommerce.service;

import com.stationery_ecommerce.common.OrderStatus;
import com.stationery_ecommerce.common.PaymentMethod;
import com.stationery_ecommerce.dto.request.OrderItemRequest;
import com.stationery_ecommerce.dto.request.OrderRequest;
import com.stationery_ecommerce.dto.response.OrderResponse;
import com.stationery_ecommerce.entity.Order;
import com.stationery_ecommerce.entity.OrderItem;
import com.stationery_ecommerce.entity.Product;
import com.stationery_ecommerce.entity.User;
import com.stationery_ecommerce.exception.payload.InsufficientStockException;
import com.stationery_ecommerce.exception.payload.ResourceNotFoundException;
import com.stationery_ecommerce.repository.OrderRepository;
import com.stationery_ecommerce.repository.ProductRepository;
import com.stationery_ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Khởi tạo các biến tính toán hóa đơn
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderResponse.OrderItemDto> responseItemDtos = new ArrayList<>();

        Order order = Order.builder()
                .user(user)
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0,8).toUpperCase())
                .status(OrderStatus.valueOf("PENDING"))
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .build();

        // Luồng xử lý từng sản phẩm - Nơi áp dụng LOCK BI QUAN
        for (OrderItemRequest itemRequest: request.getItems()) {
            Product product = productRepository.findByIdForUpdate(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + itemRequest.getProductId() + " does not exist or is no longer available for sale"));

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException("Product '" + product.getName() + "' is out of stock or there is insufficient quantity (Currently available: " + product.getStockQuantity() + ")");
            }

            // Thực hiện TRỪ KHO ĐỒNG THỜI AN TOÀN
            int newStockQuantity = product.getStockQuantity() - itemRequest.getQuantity();
            product.setStockQuantity(newStockQuantity);
            productRepository.save(product);

            // Tính toán giá tiền (Hóa đơn)
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .build();
            orderItems.add(orderItem);

            responseItemDtos.add(OrderResponse.OrderItemDto.builder()
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .build());
        }

        order.setTotalPrice(totalAmount);
        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);

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
