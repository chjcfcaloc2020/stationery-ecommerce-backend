package com.stationery_ecommerce.repository;

import com.stationery_ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT COUNT(o) FROM Order o JOIN o.orderItems oi " +
            "WHERE o.user.id = :userId AND oi.product.id = :productId AND o.status = 'DELIVERED'")
    long countDeliveredOrdersByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);
}
