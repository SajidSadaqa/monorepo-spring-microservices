package com.example.user.application.service.impl;

import com.example.user.application.dto.SignupReq;
import com.example.user.application.service.RefreshTokenService;
import com.example.user.domain.entity.RoleEntity;
import com.example.user.domain.entity.UserEntity;
import com.example.user.domain.repository.RoleRepository;
import com.example.user.infrastructure.persistence.UserJpaRepository;
import com.example.user.infrastructure.security.JwtService;
import com.example.user.interfaces.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceImplTest {

  @Mock
  private UserJpaRepository users;
  @Mock
  private RoleRepository roles;
  @Mock
  private PasswordEncoder encoder;
  @Mock
  private JwtService jwtService;
  @Mock
  private RefreshTokenService refreshTokens;

  private AuthApplicationServiceImpl authService;

  @BeforeEach
  void setUp() {
    authService = new AuthApplicationServiceImpl(users, roles, encoder, jwtService, refreshTokens);
  }

  // --- Signup Tests --- The test must be valid and invalid
  @Test
  void signup_ShouldCreateUserSuccessfully() {
    // Given
    SignupReq req = new SignupReq("testuser", "Str0ngPass!", "test@example.com");
    RoleEntity userRoleEntity = RoleEntity.builder().id(UUID.randomUUID()).name("ROLE_USER").build();
    UserEntity savedUserEntity = UserEntity.builder()
      .id(UUID.randomUUID())
      .username("testuser")
      .email("test@example.com")
      .enabled(true)
      .roleEntities(Set.of(userRoleEntity))
      .build();

    when(users.existsByUsername("testuser")).thenReturn(false);
    when(users.existsByEmail("test@example.com")).thenReturn(false);
    when(roles.findByName("ROLE_USER")).thenReturn(Optional.of(userRoleEntity));
    when(encoder.encode("Str0ngPass!")).thenReturn("hashedPassword");
    when(users.save(any(UserEntity.class))).thenReturn(savedUserEntity);
    when(jwtService.createAccess(eq("testuser"), any(), eq("user-service"), eq(List.of("user-service"))))
      .thenReturn("accessToken");
    when(refreshTokens.issueForUser(savedUserEntity)).thenReturn("refreshToken");

    // When
    var result = authService.signup(req);

    // Then
    assertThat(result.accessToken()).isEqualTo("accessToken");
    assertThat(result.refreshToken()).isEqualTo("refreshToken");
    assertThat(result.expiresInSeconds()).isEqualTo(900);
    verify(users).save(argThat(user ->
      user.getUsername().equals("testuser") &&
        user.getEmail().equals("test@example.com") &&
        user.getPasswordHash().equals("hashedPassword") &&
        user.isEnabled()));
  }

  @Test
  void signup_ShouldThrowExceptionWhenUsernameExists() {
    // Given
    SignupReq req = new SignupReq("existinguser", "Str0ngPass!", "test@example.com");
    when(users.existsByUsername("existinguser")).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> authService.signup(req))
      .isInstanceOf(BusinessException.class)
      .hasMessage("auth.username.taken");
  }

  @Test
  void signup_ShouldThrowExceptionWhenEmailExists() {
    // Given
    SignupReq req = new SignupReq("testuser", "Str0ngPass!", "existing@example.com");
    when(users.existsByUsername("testuser")).thenReturn(false);
    when(users.existsByEmail("existing@example.com")).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> authService.signup(req))
      .isInstanceOf(BusinessException.class)
      .hasMessage("auth.email.taken");
  }

  @Test
  void login_ShouldReturnTokensForValidCredentials() {
    // Given
    RoleEntity userRoleEntity = RoleEntity.builder().name("ROLE_USER").build();
    UserEntity userEntity = UserEntity.builder()
      .username("testuser")
      .passwordHash("hashedPassword")
      .enabled(true)
      .roleEntities(Set.of(userRoleEntity))
      .build();

    when(users.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
    when(encoder.matches("password", "hashedPassword")).thenReturn(true);
    when(jwtService.createAccess(eq("testuser"), any(), eq("user-service"), eq(List.of("user-service"))))
      .thenReturn("accessToken");
    when(refreshTokens.issueForUser(userEntity)).thenReturn("refreshToken");

    // When
    var result = authService.login("testuser", "password");

    // Then
    assertThat(result.accessToken()).isEqualTo("accessToken");
    assertThat(result.refreshToken()).isEqualTo("refreshToken");
    assertThat(result.expiresInSeconds()).isEqualTo(900);
  }

  @Test
  void login_ShouldThrowExceptionForInvalidUser() {
    // Given
    when(users.findByUsername("nonexistent")).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> authService.login("nonexistent", "password"))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("auth.login.invalid");
  }

  @Test
  void login_ShouldThrowExceptionForInvalidPassword() {
    // Given
    UserEntity userEntity = UserEntity.builder()
      .username("testuser")
      .passwordHash("hashedPassword")
      .enabled(true)
      .build();

    when(users.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
    when(encoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> authService.login("testuser", "wrongpassword"))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("auth.login.invalid");
  }

  @Test
  void login_ShouldThrowExceptionForDisabledUser() {
    // Given
    UserEntity userEntity = UserEntity.builder()
      .username("testuser")
      .passwordHash("hashedPassword")
      .enabled(false)
      .build();

    when(users.findByUsername("testuser")).thenReturn(Optional.of(userEntity));

    // When & Then
    assertThatThrownBy(() -> authService.login("testuser", "password"))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("auth.login.invalid");
  }

  @Test
  void refresh_ShouldReturnNewTokens() {
    // Given
    String refreshToken = "refreshToken";
    Jwt jwt = mock(Jwt.class);
    RoleEntity userRoleEntity = RoleEntity.builder().name("ROLE_USER").build();
    UserEntity userEntity = UserEntity.builder()
      .username("testuser")
      .roleEntities(Set.of(userRoleEntity))
      .build();

    when(jwtService.decode(refreshToken)).thenReturn(jwt);
    when(jwt.getClaims()).thenReturn(java.util.Map.of("typ", "refresh"));
    when(jwt.getSubject()).thenReturn("testuser");
    when(users.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
    when(jwtService.createAccess(eq("testuser"), any(), eq("user-service"), eq(List.of("user-service"))))
      .thenReturn("newAccessToken");
    when(refreshTokens.rotate(refreshToken)).thenReturn("newRefreshToken");

    // When
    var result = authService.refresh(refreshToken);

    // Then
    assertThat(result.accessToken()).isEqualTo("newAccessToken");
    assertThat(result.refreshToken()).isEqualTo("newRefreshToken");
    assertThat(result.expiresInSeconds()).isEqualTo(900);
  }

  @Test
  void refresh_ShouldThrowExceptionForNonRefreshToken() {
    // Given
    String accessToken = "accessToken";
    Jwt jwt = mock(Jwt.class);

    when(jwtService.decode(accessToken)).thenReturn(jwt);
    when(jwt.getClaims()).thenReturn(java.util.Map.of("typ", "access"));

    // When & Then
    assertThatThrownBy(() -> authService.refresh(accessToken))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("jwt.refresh.required");
  }
}
