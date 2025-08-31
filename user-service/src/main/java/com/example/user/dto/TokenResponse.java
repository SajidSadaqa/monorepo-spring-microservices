package com.example.user.dto;

public record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds) {}
