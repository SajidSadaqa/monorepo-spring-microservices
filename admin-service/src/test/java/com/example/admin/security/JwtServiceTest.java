package com.example.admin.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtServiceTest {

  @Autowired JwtService jwtService;

  @Test
  void mintS2S_containsTypAndAudience() {
    String token = jwtService.mintS2S("admin-service", "admin-service", "user-service", List.of("ROLE_ADMIN"));
    assertThat(token).isNotBlank();
  }
}
