package com.microservices.user.application.service;

import com.microservices.user.application.dto.SignupReq;
import com.microservices.user.application.dto.TokenResponse;

public interface AuthApplicationService {
  TokenResponse signup(SignupReq req);
  TokenResponse login(String username, String password);
  TokenResponse refresh(String refreshToken);
}
