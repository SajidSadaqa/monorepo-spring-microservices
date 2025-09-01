package com.example.admin.application.dto.response;

public record TokenResDto(String accessToken, String refreshToken, long expiresInSeconds) {}
