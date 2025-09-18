package com.microservices.user.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(900, 604800,
      "0123456789ABCDEF0123456789ABCDEF", "");
  }

  @Test
  void createAccess_ShouldCreateValidToken() {
    // Given
    String subject = "testuser";
    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    String issuer = "user-service";
    List<String> audience = List.of("user-service");

    // When
    String token = jwtService.createAccess(subject, authorities, issuer, audience);

    // Then
    assertThat(token).isNotNull().isNotBlank();

    Jwt jwt = jwtService.decode(token);
    assertThat(jwt.getSubject()).isEqualTo(subject);
    assertThat((String)jwt.getClaim("iss")).isEqualTo(issuer);
    assertThat(jwt.getAudience()).contains("user-service");
    assertThat((List<?>) jwt.getClaim("roles")).isEqualTo(List.of("ROLE_USER"));

    // Verify token structure
    assertThat(jwt.getIssuedAt()).isNotNull();
    assertThat(jwt.getExpiresAt()).isNotNull();
    assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
  }

  @Test
  void createRefresh_ShouldCreateValidRefreshToken() {
    // Given
    String subject = "testuser";
    String issuer = "user-service";

    // When
    String token = jwtService.createRefresh(subject, issuer);

    // Then
    assertThat(token).isNotNull().isNotBlank();

    Jwt jwt = jwtService.decode(token);
    assertThat(jwt.getSubject()).isEqualTo(subject);
    assertThat((String)jwt.getClaim("iss")).isEqualTo(issuer);
    assertThat((String)jwt.getClaim("typ")).isEqualTo("refresh");
    assertThat(jwt.getId()).isNotNull();

    // Verify token structure
    assertThat(jwt.getIssuedAt()).isNotNull();
    assertThat(jwt.getExpiresAt()).isNotNull();
    assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
  }

  @Test
  void createRefreshWithJti_ShouldUseProvidedJti() {
    // Given
    String subject = "testuser";
    String issuer = "user-service";
    String jti = "custom-jti";

    // When
    String token = jwtService.createRefreshWithJti(subject, issuer, jti);

    // Then
    Jwt jwt = jwtService.decode(token);
    assertThat(jwt.getId()).isEqualTo(jti);
    assertThat(jwt.getSubject()).isEqualTo(subject);
    assertThat((String)jwt.getClaim("iss")).isEqualTo(issuer);
    assertThat((String)jwt.getClaim("typ")).isEqualTo("refresh");
  }

  @Test
  void mintS2STokenForUserService_ShouldCreateValidS2SToken() {
    // When
    String token = jwtService.mintS2STokenForUserService();

    // Then
    assertThat(token).isNotNull().isNotBlank();

    Jwt jwt = jwtService.decode(token);
    assertThat((String)jwt.getClaim("iss")).isEqualTo("admin-service");
    assertThat(jwt.getSubject()).isEqualTo("admin-service");
    assertThat(jwt.getAudience()).contains("user-service");
    assertThat((Boolean) jwt.getClaim("s2s")).isEqualTo(true);
    assertThat((List<?>) jwt.getClaim("scope")).isEqualTo(List.of("s2s"));

    // Verify S2S token expiration (short-lived)
    assertThat(jwt.getIssuedAt()).isNotNull();
    assertThat(jwt.getExpiresAt()).isNotNull();
    assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
  }

  @Test
  void createAccess_ShouldHandleNullAuthorities() {
    // Given
    String subject = "testuser";
    String issuer = "user-service";
    List<String> audience = List.of("user-service");

    // When & Then - This should throw an exception due to null roleEntities
    assertThatThrownBy(() -> jwtService.createAccess(subject, null, issuer, audience))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("value cannot be null");
  }

  @Test
  void createAccess_ShouldHandleEmptyAuthorities() {
    // Given
    String subject = "testuser";
    List<SimpleGrantedAuthority> authorities = List.of();
    String issuer = "user-service";
    List<String> audience = List.of("user-service");

    // When
    String token = jwtService.createAccess(subject, authorities, issuer, audience);

    // Then
    Jwt jwt = jwtService.decode(token);
    assertThat((List<?>) jwt.getClaim("roles")).isEmpty();
  }

  @Test
  void createAccess_ShouldHandleMultipleRoles() {
    // Given
    String subject = "admin";
    List<SimpleGrantedAuthority> authorities = List.of(
      new SimpleGrantedAuthority("ROLE_USER"),
      new SimpleGrantedAuthority("ROLE_ADMIN")
    );
    String issuer = "user-service";
    List<String> audience = List.of("user-service");

    // When
    String token = jwtService.createAccess(subject, authorities, issuer, audience);

    // Then
    Jwt jwt = jwtService.decode(token);
    assertThat((List<?>) jwt.getClaim("roles")).isEqualTo(List.of("ROLE_USER", "ROLE_ADMIN"));
  }

  @Test
  void createAccess_ShouldHandleMultipleAudiences() {
    // Given
    String subject = "testuser";
    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    String issuer = "user-service";
    List<String> audience = List.of("user-service", "admin-service");

    // When
    String token = jwtService.createAccess(subject, authorities, issuer, audience);

    // Then
    Jwt jwt = jwtService.decode(token);
    assertThat(jwt.getAudience()).containsExactlyInAnyOrder("user-service", "admin-service");
  }

  @Test
  void decode_ShouldThrowExceptionForInvalidToken() {
    // Given
    String invalidToken = "invalid.jwt.token";

    // When & Then
    assertThatThrownBy(() -> jwtService.decode(invalidToken))
      .isInstanceOf(RuntimeException.class);
  }

  @Test
  void decode_ShouldThrowExceptionForMalformedToken() {
    // Given
    String malformedToken = "not-a-jwt-at-all";

    // When & Then
    assertThatThrownBy(() -> jwtService.decode(malformedToken))
      .isInstanceOf(RuntimeException.class);
  }

  @Test
  void decode_ShouldThrowExceptionForEmptyToken() {
    // Given
    String emptyToken = "";

    // When & Then
    assertThatThrownBy(() -> jwtService.decode(emptyToken))
      .isInstanceOf(RuntimeException.class);
  }
}
