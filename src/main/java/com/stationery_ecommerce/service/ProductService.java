package com.stationery_ecommerce.service;

import com.stationery_ecommerce.dto.request.ProductRequest;
import com.stationery_ecommerce.dto.response.ProductResponse;
import com.stationery_ecommerce.entity.Category;
import com.stationery_ecommerce.entity.Product;
import com.stationery_ecommerce.exception.payload.ResourceAlreadyExistsException;
import com.stationery_ecommerce.exception.payload.ResourceNotFoundException;
import com.stationery_ecommerce.repository.CategoryRepository;
import com.stationery_ecommerce.repository.ProductRepository;
import com.stationery_ecommerce.util.HelperMethod;
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
        return productRepository.findByCategoryIdAndIsAvailableTrue(categoryId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndIsAvailableTrue(keyword, pageable)
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
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String slug = HelperMethod.generateSlug(request.getName());
        if (productRepository.existsBySlug(slug)) {
            slug += "-" + System.currentTimeMillis() % 1000;
        }

        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .stockQuantity(request.getStockQuantity())
                .tags(request.getTags())
                .isAvailable(true)
                .isNew(true)
                .isBestSeller(false)
                .isFeatured(false)
                .isOnSale(false)
                .brand(request.getBrand())
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

        product.setName(request.getName());
        product.setSlug(HelperMethod.generateSlug(request.getName()));
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setTags(request.getTags());
        product.setBrand(request.getBrand());
        product.setNew(request.isNew());
        product.setBestSeller(request.isBestSeller());
        product.setFeatured(request.isFeatured());
        product.setOnSale(request.isOnSale());
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
                .categoryName(product.getCategory().getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .stockQuantity(product.getStockQuantity())
                .rating(product.getRating())
                .imageUrl(product.getImageUrl())
                .images(product.getImages())
                .tags(product.getTags())
                .isAvailable(product.isAvailable())
                .isNew(product.isNew())
                .isBestSeller(product.isBestSeller())
                .isFeatured(product.isFeatured())
                .isOnSale(product.isOnSale())
                .colors(product.getColors())
                .brand(product.getBrand())
                .build();
    }
}
