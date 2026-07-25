package com.stationery_ecommerce.service;

import com.stationery_ecommerce.dto.request.ReviewRequest;
import com.stationery_ecommerce.dto.response.ReviewResponse;
import com.stationery_ecommerce.entity.Product;
import com.stationery_ecommerce.entity.Review;
import com.stationery_ecommerce.entity.User;
import com.stationery_ecommerce.exception.payload.ResourceNotFoundException;
import com.stationery_ecommerce.exception.payload.ReviewException;
import com.stationery_ecommerce.repository.OrderRepository;
import com.stationery_ecommerce.repository.ProductRepository;
import com.stationery_ecommerce.repository.ReviewRepository;
import com.stationery_ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    private User getCurrentUser() {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // 1. VIẾT ĐÁNH GIÁ MỚI (Có kiểm tra điều kiện đã mua hàng)
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        User user = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product does not exist in system"));

        long deliveredOrdersCount = orderRepository.countDeliveredOrdersByUserAndProduct(user.getId(), product.getId());
        if (deliveredOrdersCount == 0) {
            throw new ReviewException("You just can review this product after bought and received successfully! (Status of order is DELIVERED)!");
        }

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new ReviewException("You have already submitted a review for this product!");
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment().trim())
                .build();

        Review savedReview = reviewRepository.save(review);
        return mapToResponse(savedReview);
    }

    // 2. LẤY DANH SÁCH ĐÁNH GIÁ CỦA 1 SẢN PHẨM (Public cho tất cả mọi người xem)
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product does not exist");
        }

        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 3. ADMIN XÓA BÌNH LUẬN SPAM / VI PHẠM
    @Transactional
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review not found with ID: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    private ReviewResponse mapToResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .productId(r.getProduct().getId())
                .userId(r.getUser().getId())
                .userFullName(r.getUser().getFullName())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
