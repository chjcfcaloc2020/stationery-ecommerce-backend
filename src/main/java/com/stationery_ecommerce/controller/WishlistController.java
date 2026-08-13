package com.stationery_ecommerce.controller;

import com.stationery_ecommerce.dto.request.WishlistRequest;
import com.stationery_ecommerce.dto.response.WishlistResponse;
import com.stationery_ecommerce.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    public ResponseEntity<WishlistResponse> addToWishlist(@Valid @RequestBody WishlistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wishlistService.addToWishlist(request));
    }

    @GetMapping
    public ResponseEntity<List<WishlistResponse>> getMyWishlist() {
        return ResponseEntity.ok(wishlistService.getMyWishlist());
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long productId) {
        wishlistService.removeFromWishlist(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<Boolean> isWishlisted(@PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.isWishlisted(productId));
    }
}
