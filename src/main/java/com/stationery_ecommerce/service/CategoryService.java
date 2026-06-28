package com.stationery_ecommerce.service;

import com.stationery_ecommerce.dto.request.CategoryRequest;
import com.stationery_ecommerce.dto.response.CategoryResponse;
import com.stationery_ecommerce.entity.Category;
import com.stationery_ecommerce.exception.payload.ResourceAlreadyExistsException;
import com.stationery_ecommerce.exception.payload.ResourceNotFoundException;
import com.stationery_ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return mapToResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Category already exists");
        }

        String slug = generateSlug(request.getName());
        if (categoryRepository.existsBySlug(slug)) {
            slug += "-" + System.currentTimeMillis() % 1000;
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setName(request.getName());
        category.setSlug(generateSlug(request.getName()));

        return mapToResponse(categoryRepository.save(category));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    // -- Helper Methods
    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .build();
    }

    private String generateSlug(String input) {
        if (input == null || input.isEmpty()) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

        String slug = pattern.matcher(normalized).replaceAll("").toLowerCase(Locale.ROOT);
        return slug.replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}
