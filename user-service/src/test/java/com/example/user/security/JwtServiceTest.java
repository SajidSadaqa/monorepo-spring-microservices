package com.example.user.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  @Test
  void createAndDecodeTokens_ok() {
    String secret = "0123456789ABCDEF0123456789ABCDEF"; // 32-byte test secret
    JwtService svc = new JwtService(900, 604800, secret);

    String access = svc.createAccess("sajid", List.of(), "user-service", List.of("user-service"));
    assertThat(access).isNotBlank();
    var jwt = svc.decode(access);
    assertThat(jwt.getSubject()).isEqualTo("sajid");
  }

}
