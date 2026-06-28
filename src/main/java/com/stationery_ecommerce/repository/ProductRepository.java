package com.stationery_ecommerce.repository;

import com.stationery_ecommerce.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategoryIdAndIsAvaiableTrue(Long categoryId, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndIsAvaiableTrue(String name, Pageable pageable);
    boolean existsBySku(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.isAvailable = true")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
