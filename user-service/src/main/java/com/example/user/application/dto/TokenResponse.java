package com.example.user.application.dto;

public record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds) {}
