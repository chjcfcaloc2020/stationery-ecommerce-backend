package com.stationery_ecommerce.service;

import com.stationery_ecommerce.dto.request.WishlistRequest;
import com.stationery_ecommerce.dto.response.WishlistResponse;
import com.stationery_ecommerce.entity.Product;
import com.stationery_ecommerce.entity.User;
import com.stationery_ecommerce.entity.WishlistItem;
import com.stationery_ecommerce.exception.payload.ResourceNotFoundException;
import com.stationery_ecommerce.repository.ProductRepository;
import com.stationery_ecommerce.repository.UserRepository;
import com.stationery_ecommerce.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private User getCurrentUser() {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public WishlistResponse addToWishlist(WishlistRequest request) {
        User user = getCurrentUser();

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        // Tránh lưu trùng lặp: Nếu đã yêu thích rồi thì trả về thông tin item cũ
        if (wishlistRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            WishlistItem existingItem = wishlistRepository.findByUserIdAndProductId(user.getId(), product.getId()).get();
            return mapToResponse(existingItem);
        }

        WishlistItem item = WishlistItem.builder()
                .user(user)
                .product(product)
                .build();

        return mapToResponse(wishlistRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<WishlistResponse> getMyWishlist() {
        User user = getCurrentUser();
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFromWishlist(Long productId) {
        User user = getCurrentUser();
        if (!wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new ResourceNotFoundException("Product is not in the wishlist");
        }
        wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    @Transactional(readOnly = true)
    public boolean isWishlisted(Long productId) {
        User user = getCurrentUser();
        return wishlistRepository.existsByUserIdAndProductId(user.getId(), productId);
    }

    private WishlistResponse mapToResponse(WishlistItem item) {
        Product p = item.getProduct();
        return WishlistResponse.builder()
                .id(item.getId())
                .productId(p.getId())
                .productName(p.getName())
                .slug(p.getSlug())
                .price(p.getPrice())
                .rating(p.getRating())
                .reviewCount(p.getReviewCount())
                .imageUrl(p.getImageUrl())
                .brand(p.getBrand())
                .isNew(p.isNew())
                .isBestSeller(p.isBestSeller())
                .isFeatured(p.isFeatured())
                .isOnSale(p.isOnSale())
                .build();
    }
}
