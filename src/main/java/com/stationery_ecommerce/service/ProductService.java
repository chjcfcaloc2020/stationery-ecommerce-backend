package com.stationery_ecommerce.service;

import com.stationery_ecommerce.dto.request.ProductRequest;
import com.stationery_ecommerce.dto.response.ProductResponse;
import com.stationery_ecommerce.entity.Category;
import com.stationery_ecommerce.entity.Product;
import com.stationery_ecommerce.exception.payload.ResourceAlreadyExistsException;
import com.stationery_ecommerce.exception.payload.ResourceNotFoundException;
import com.stationery_ecommerce.repository.CategoryRepository;
import com.stationery_ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndIsAvaiableTrue(categoryId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndIsAvaiableTrue(keyword, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceAlreadyExistsException("Product is not exists"));
        return mapToResponse(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new ResourceAlreadyExistsException("The product SKU already exists in stock");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .sku(request.getSku())
                .isAvailable(true)
                .category(category)
                .build();

        return mapToResponse(productRepository.save(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Nếu thay đổi SKU, cần check trùng với các sản phẩm khác
        if (!product.getSku().equals(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new ResourceAlreadyExistsException("New Code SKU is exists");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setCategory(category);

        return mapToResponse(productRepository.save(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Soft delete (Xóa mềm) để bảo vệ báo cáo doanh thu và đơn hàng liên quan
        product.setAvailable(false);
        productRepository.save(product);
    }

    // -- Helper Methods
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .sku(product.getSku())
                .isAvailable(product.isAvailable())
                .categoryName(product.getCategory().getName())
                .build();
    }
}
