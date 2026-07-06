package com.stationery_ecommerce.exception.payload;

public class TokenRefreshException extends RuntimeException {
    public TokenRefreshException(String token, String message) {
        super(String.format("Error! authenticated token [%s]: %s", token, message));
    }
}
