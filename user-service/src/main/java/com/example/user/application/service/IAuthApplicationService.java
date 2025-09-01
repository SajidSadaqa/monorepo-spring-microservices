package com.example.user.application.service;

import com.example.user.application.dto.SignupReq;
import com.example.user.application.dto.TokenResponse;

public interface IAuthApplicationService {
  TokenResponse signup(SignupReq req);
  TokenResponse login(String username, String password);
  TokenResponse refresh(String refreshToken);
}
