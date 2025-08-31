package com.example.admin.service;

import com.example.admin.dto.TokenResponse;

public interface AdminAuthService {
  TokenResponse login(String username, String password);
}
