package com.example.admin.dto;

public record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds) {}
