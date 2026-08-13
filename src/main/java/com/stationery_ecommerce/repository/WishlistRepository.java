package com.stationery_ecommerce.repository;

import com.stationery_ecommerce.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    // Kiểm tra xem sản phẩm đã có trong wishlist của user chưa
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    List<WishlistItem> findByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteByUserIdAndProductId(Long userId, Long productId);
    Optional<WishlistItem> findByUserIdAndProductId(Long userId, Long productId);
}
