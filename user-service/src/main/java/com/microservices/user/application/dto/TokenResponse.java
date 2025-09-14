package com.microservices.user.application.dto;

public record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds) {}
