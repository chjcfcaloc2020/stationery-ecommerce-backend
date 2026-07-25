package com.stationery_ecommerce.repository;

import com.stationery_ecommerce.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    // Kiểm tra xem người dùng này đã đánh giá sản phẩm này chưa (Tránh 1 người spam 100 review cho 1 lần mua)
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
