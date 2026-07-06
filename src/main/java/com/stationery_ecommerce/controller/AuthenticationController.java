package com.stationery_ecommerce.controller;

import com.stationery_ecommerce.dto.request.AuthenticationRequest;
import com.stationery_ecommerce.dto.request.RegisterRequest;
import com.stationery_ecommerce.dto.request.TokenRefreshRequest;
import com.stationery_ecommerce.dto.response.AuthenticationResponse;
import com.stationery_ecommerce.dto.response.TokenRefreshResponse;
import com.stationery_ecommerce.entity.RefreshToken;
import com.stationery_ecommerce.exception.payload.TokenRefreshException;
import com.stationery_ecommerce.service.AuthenticationService;
import com.stationery_ecommerce.service.JwtService;
import com.stationery_ecommerce.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthenticationController {

    private final AuthenticationService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshAccessToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                    String newAccessToken = jwtService.generateToken(userDetails);

                    return ResponseEntity.ok(TokenRefreshResponse.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(requestRefreshToken)
                            .build());
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh Token didn't exist in system!"));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser() {
        authService.logout();
        return ResponseEntity.ok("Logout successfully!");
    }
}
