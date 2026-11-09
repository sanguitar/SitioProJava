package com.example.sitiopro.usuario.mobile.dto;

public record MobileTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long refreshExpiresIn,
        MobileUserResponse user) {
}
