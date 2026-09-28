package com.stationery_ecommerce.service;

import com.stationery_ecommerce.entity.RefreshToken;
import com.stationery_ecommerce.exception.payload.ResourceNotFoundException;
import com.stationery_ecommerce.exception.payload.TokenRefreshException;
import com.stationery_ecommerce.repository.RefreshTokenRepository;
import com.stationery_ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-token-duration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    /**
     * Save Refresh Token into Redis with (TTL)
     * Key: RT:{username}
     * Value: refreshToken
     */
    public void saveRefreshToken(String username, String refreshToken, long expirationMs) {
        String key = REFRESH_TOKEN_PREFIX + username;
        stringRedisTemplate.opsForValue().set(key, refreshToken, Duration.ofMillis(expirationMs));
    }

    public boolean validateRefreshToken(String username, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + username;
        String storedToken = stringRedisTemplate.opsForValue().get(key);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    public void deleteRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        stringRedisTemplate.delete(key);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found")))
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Token was expired. Please login again!.");
        }

        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        userRepository.findById(userId).ifPresent(refreshTokenRepository::deleteByUser);
    }
}
