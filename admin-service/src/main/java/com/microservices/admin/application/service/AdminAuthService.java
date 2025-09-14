package com.microservices.admin.application.service;

import com.microservices.admin.application.dto.response.TokenResDto;

public interface AdminAuthService {
  TokenResDto login(String username, String password);
}
