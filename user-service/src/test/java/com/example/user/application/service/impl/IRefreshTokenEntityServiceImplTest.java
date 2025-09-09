// src/test/java/com/example/userEntity/application/service/impl/IRefreshTokenEntityServiceImplTest.java
package com.example.user.application.service.impl;

import com.example.user.domain.entity.RefreshTokenEntity;
import com.example.user.domain.entity.UserEntity;
import com.example.user.domain.repository.RefreshTokenRepository;
import com.example.user.infrastructure.persistence.UserJpaRepository;
import com.example.user.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IRefreshTokenEntityServiceImplTest {

  @Mock
  private RefreshTokenRepository tokens;
  @Mock
  private UserJpaRepository users;
  @Mock
  private JwtService jwt;

  private RefreshTokenServiceImpl refreshTokenService;

  @BeforeEach
  void setUp() {
    refreshTokenService = new RefreshTokenServiceImpl(tokens, users, jwt);
  }

  @Test
  void issueForUser_ShouldCreateAndSaveToken() {
    // Given
    UserEntity userEntity = UserEntity.builder().id(UUID.randomUUID()).username("testuser").build();
    String tokenString = "jwt-token";
    Jwt parsedJwt = mock(Jwt.class);
    Instant expiresAt = Instant.now().plusSeconds(3600);

    // Fix: Use eq() matchers for all arguments
    when(jwt.createRefreshWithJti(eq("testuser"), eq("user-service"), anyString())).thenReturn(tokenString);
    when(jwt.decode(tokenString)).thenReturn(parsedJwt);
    when(parsedJwt.getExpiresAt()).thenReturn(expiresAt);

    // When
    String result = refreshTokenService.issueForUser(userEntity);

    // Then
    assertThat(result).isEqualTo(tokenString);
    verify(tokens).save(argThat(token ->
      token.getUserEntity().equals(userEntity) &&
        token.getExpiresAt().equals(expiresAt) &&
        !token.isRevoked()));
  }

  @Test
  void rotate_ShouldRevokeOldAndCreateNew() {
    // Given
    String currentToken = "current-token";
    String newToken = "new-token";

    UserEntity userEntity = UserEntity.builder().id(UUID.randomUUID()).username("testuser").build();

    Jwt currentJwt = mock(Jwt.class);
    Jwt newJwt = mock(Jwt.class);

    RefreshTokenEntity storedToken = RefreshTokenEntity.builder()
      .jti("old-jti")
      .userEntity(userEntity)
      .expiresAt(Instant.now().plusSeconds(3600))
      .revoked(false)
      .build();

    when(jwt.decode(currentToken)).thenReturn(currentJwt);
    when(currentJwt.getClaims()).thenReturn(java.util.Map.of("typ", "refresh"));
    when(currentJwt.getSubject()).thenReturn("testuser");
    when(currentJwt.getId()).thenReturn("old-jti");
    when(users.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
    when(tokens.findByJti("old-jti")).thenReturn(Optional.of(storedToken));

    // Fix: Use eq() matchers for all arguments
    when(jwt.createRefreshWithJti(eq("testuser"), eq("user-service"), anyString())).thenReturn(newToken);
    when(jwt.decode(newToken)).thenReturn(newJwt);
    when(newJwt.getExpiresAt()).thenReturn(Instant.now().plusSeconds(3600));

    // When
    String result = refreshTokenService.rotate(currentToken);

    // Then
    assertThat(result).isEqualTo(newToken);
    assertThat(storedToken.isRevoked()).isTrue();
    verify(tokens).save(storedToken);
    verify(tokens).save(argThat(token ->
      token.getUserEntity().equals(userEntity) &&
        !token.isRevoked()));
  }

  @Test
  void rotate_ShouldThrowExceptionForRevokedToken() {
    // Given
    String currentToken = "revoked-token";

    UserEntity userEntity = UserEntity.builder().username("testuser").build();
    Jwt currentJwt = mock(Jwt.class);
    RefreshTokenEntity revokedToken = RefreshTokenEntity.builder()
      .jti("revoked-jti")
      .revoked(true)
      .build();

    when(jwt.decode(currentToken)).thenReturn(currentJwt);
    when(currentJwt.getClaims()).thenReturn(java.util.Map.of("typ", "refresh"));
    when(currentJwt.getSubject()).thenReturn("testuser");
    when(currentJwt.getId()).thenReturn("revoked-jti");
    when(users.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
    when(tokens.findByJti("revoked-jti")).thenReturn(Optional.of(revokedToken));

    // When & Then
    assertThatThrownBy(() -> refreshTokenService.rotate(currentToken))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("refresh.revoked");
  }

  @Test
  void rotate_ShouldThrowExceptionForExpiredToken() {
    // Given
    String currentToken = "expired-token";

    UserEntity userEntity = UserEntity.builder().username("testuser").build();
    Jwt currentJwt = mock(Jwt.class);
    RefreshTokenEntity expiredToken = RefreshTokenEntity.builder()
      .jti("expired-jti")
      .expiresAt(Instant.now().minusSeconds(3600)) // Expired
      .revoked(false)
      .build();

    when(jwt.decode(currentToken)).thenReturn(currentJwt);
    when(currentJwt.getClaims()).thenReturn(java.util.Map.of("typ", "refresh"));
    when(currentJwt.getSubject()).thenReturn("testuser");
    when(currentJwt.getId()).thenReturn("expired-jti");
    when(users.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
    when(tokens.findByJti("expired-jti")).thenReturn(Optional.of(expiredToken));

    // When & Then
    assertThatThrownBy(() -> refreshTokenService.rotate(currentToken))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("refresh.expired");
  }

  @Test
  void rotate_ShouldThrowExceptionForInvalidTokenType() {
    // Given
    String currentToken = "access-token";
    Jwt currentJwt = mock(Jwt.class);

    when(jwt.decode(currentToken)).thenReturn(currentJwt);
    when(currentJwt.getClaims()).thenReturn(java.util.Map.of("typ", "access")); // Wrong type

    // When & Then
    assertThatThrownBy(() -> refreshTokenService.rotate(currentToken))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("jwt.refresh.required");
  }

  @Test
  void rotate_ShouldThrowExceptionForMissingJti() {
    // Given
    String currentToken = "no-jti-token";
    UserEntity userEntity = UserEntity.builder().username("testuser").build();
    Jwt currentJwt = mock(Jwt.class);

    when(jwt.decode(currentToken)).thenReturn(currentJwt);
    when(currentJwt.getClaims()).thenReturn(java.util.Map.of("typ", "refresh"));
    when(currentJwt.getSubject()).thenReturn("testuser");
    when(currentJwt.getId()).thenReturn(null); // Missing JTI

    // When & Then
    assertThatThrownBy(() -> refreshTokenService.rotate(currentToken))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("jwt.invalid");
  }

  @Test
  void rotate_ShouldThrowExceptionForNonexistentUser() {
    // Given
    String currentToken = "valid-token";
    Jwt currentJwt = mock(Jwt.class);

    when(jwt.decode(currentToken)).thenReturn(currentJwt);
    when(currentJwt.getClaims()).thenReturn(java.util.Map.of("typ", "refresh"));
    when(currentJwt.getSubject()).thenReturn("nonexistent");
    when(currentJwt.getId()).thenReturn("valid-jti");
    when(users.findByUsername("nonexistent")).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> refreshTokenService.rotate(currentToken))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("auth.login.invalid");
  }

  @Test
  void rotate_ShouldThrowExceptionForNonexistentToken() {
    // Given
    String currentToken = "unknown-token";
    UserEntity userEntity = UserEntity.builder().username("testuser").build();
    Jwt currentJwt = mock(Jwt.class);

    when(jwt.decode(currentToken)).thenReturn(currentJwt);
    when(currentJwt.getClaims()).thenReturn(java.util.Map.of("typ", "refresh"));
    when(currentJwt.getSubject()).thenReturn("testuser");
    when(currentJwt.getId()).thenReturn("unknown-jti");
    when(users.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
    when(tokens.findByJti("unknown-jti")).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> refreshTokenService.rotate(currentToken))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("refresh.invalid");
  }

  @Test
  void revokeAllForUser_ShouldRevokeAllActiveTokens() {
    // Given
    UserEntity userEntity = UserEntity.builder().id(UUID.randomUUID()).build();
    RefreshTokenEntity token1 = RefreshTokenEntity.builder().revoked(false).build();
    RefreshTokenEntity token2 = RefreshTokenEntity.builder().revoked(false).build();
    List<RefreshTokenEntity> activeTokens = List.of(token1, token2);

    when(tokens.findByUser_IdAndRevokedFalse(userEntity.getId())).thenReturn(activeTokens);

    // When
    refreshTokenService.revokeAllForUser(userEntity);

    // Then
    assertThat(token1.isRevoked()).isTrue();
    assertThat(token2.isRevoked()).isTrue();
    verify(tokens).saveAll(activeTokens);
  }

  @Test
  void revokeAllForUser_ShouldHandleEmptyTokenList() {
    // Given
    UserEntity userEntity = UserEntity.builder().id(UUID.randomUUID()).build();
    List<RefreshTokenEntity> emptyTokens = List.of();

    when(tokens.findByUser_IdAndRevokedFalse(userEntity.getId())).thenReturn(emptyTokens);

    // When
    refreshTokenService.revokeAllForUser(userEntity);

    // Then
    verify(tokens).saveAll(emptyTokens);
  }
}
