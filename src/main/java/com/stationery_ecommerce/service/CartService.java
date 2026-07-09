package com.stationery_ecommerce.service;

import com.stationery_ecommerce.dto.request.CartItemRequest;
import com.stationery_ecommerce.dto.response.CartResponse;
import com.stationery_ecommerce.entity.CartItem;
import com.stationery_ecommerce.entity.Product;
import com.stationery_ecommerce.entity.User;
import com.stationery_ecommerce.exception.payload.InsufficientStockException;
import com.stationery_ecommerce.exception.payload.ResourceNotFoundException;
import com.stationery_ecommerce.repository.CartItemRepository;
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

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        User user = getCurrentUser();
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        BigDecimal totalCartAmount = BigDecimal.ZERO;
        List<CartResponse.CartItemDto> itemDtos = new ArrayList<>();

        for (CartItem item : cartItems) {
            BigDecimal subTotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalCartAmount = totalCartAmount.add(subTotal);

            itemDtos.add(CartResponse.CartItemDto.builder()
                    .cartItemId(item.getId())
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .price(item.getProduct().getPrice())
                    .quantity(item.getQuantity())
                    .subTotal(subTotal)
                    .build());
        }

        return CartResponse.builder()
                .items(itemDtos)
                .totalCartAmount(totalCartAmount)
                .build();
    }

    @Transactional
    public CartResponse addItemToCart(CartItemRequest request) {
        User user = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElse(CartItem.builder().user(user).product(product).quantity(0).build());

        int targetQuantity = cartItem.getQuantity() + request.getQuantity();

        if (product.getStockQuantity() < targetQuantity) {
            throw new InsufficientStockException("Product '" + product.getName() + "' is out of stock or there is insufficient quantity (Currently available: " + product.getStockQuantity() + ")");
        }

        cartItem.setQuantity(targetQuantity);
        cartItemRepository.save(cartItem);

        return getCart();
    }

    @Transactional
    public CartResponse updateItemQuantity(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem not found"));

        if (cartItem.getProduct().getStockQuantity() < quantity) {
            throw new InsufficientStockException("The warehouse does not have the required quantity (Currently available: " + cartItem.getProduct().getStockQuantity() + ")");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return getCart();
    }

    @Transactional
    public CartResponse removeItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem not found"));

        cartItemRepository.delete(cartItem);
        return getCart();
    }

    @Transactional
    public void clearCart() {
        User user = getCurrentUser();
        cartItemRepository.deleteByUser(user);
    }
}
