package com.example.admin.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.example.admin.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtTokenServiceTest {

  @Autowired
  JwtTokenService jwtTokenService;

  @Test
  void mintS2S_containsTypAndAudience() {
    String token = jwtTokenService.mintS2S("admin-service", "admin-service", "user-service", List.of("ROLE_ADMIN"));
    assertThat(token).isNotBlank();
  }
}
