package com.example.user.service;

import com.example.user.dto.SignupReq;
import com.example.user.dto.TokenResponse;

public interface AuthService {
  TokenResponse signup(SignupReq req);
  TokenResponse login(String username, String password);
  TokenResponse refresh(String refreshToken);
}
