package com.example.admin.application.service;

import com.example.admin.application.dto.response.TokenResDto;

public interface AdminAuthService {
  TokenResDto login(String username, String password);
}
