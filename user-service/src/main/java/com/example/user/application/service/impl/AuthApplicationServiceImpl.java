package com.example.user.application.service.impl;

import com.example.user.application.service.RefreshTokenService;
import com.example.user.domain.entity.RoleEntity;
import com.example.user.domain.entity.UserEntity;
import com.example.user.application.dto.SignupReq;
import com.example.user.application.dto.TokenResponse;
import com.example.user.interfaces.exception.BusinessException;
import com.example.user.domain.repository.RoleRepository;
import com.example.user.infrastructure.persistence.UserJpaRepository;
import com.example.user.infrastructure.security.JwtService;
import com.example.user.application.service.AuthApplicationService;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthApplicationServiceImpl implements AuthApplicationService {

  private final UserJpaRepository users;
  private final RoleRepository roles;
  private final PasswordEncoder encoder;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokens;

  @Override
  @Transactional
  public TokenResponse signup(SignupReq req) {
    log.info("Starting user signup process for username: {}", req.username());

    try {
      if (users.existsByUsername(req.username())) {
        log.warn("Signup failed - username already taken: {}", req.username());
        throw new BusinessException("USERNAME_TAKEN", "auth.username.taken");
      }

      if (users.existsByEmail(req.email())) {
        log.warn("Signup failed - email already taken: {}", req.email());
        throw new BusinessException("EMAIL_TAKEN", "auth.email.taken");
      }

      log.debug("Creating user role for new user: {}", req.username());
      RoleEntity userRoleEntity = roles.findByName("ROLE_USER")
        .orElseGet(() -> {
          log.info("Creating new ROLE_USER entity");
          return roles.save(RoleEntity.builder().name("ROLE_USER").build());
        });

      log.debug("Building user entity for: {}", req.username());
      UserEntity u = UserEntity.builder()
        .username(req.username())
        .email(req.email())
        .passwordHash(encoder.encode(req.password()))
        .enabled(true)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .roleEntities(Set.of(userRoleEntity))
        .build();

      u = users.save(u);
      log.info("User created successfully with ID: {} for username: {}", u.getId(), req.username());

      var auths = List.of(new SimpleGrantedAuthority("ROLE_USER"));
      log.debug("Generating access token for user: {}", req.username());
      String access = jwtService.createAccess(u.getUsername(), auths, "user-service", List.of("user-service"));

      log.debug("Generating refresh token for user: {}", req.username());
      String refresh = refreshTokens.issueForUser(u);

      log.info("Signup completed successfully for user: {}", req.username());
      return new TokenResponse(access, refresh, 900);

    } catch (Exception e) {
      log.error("Signup failed for username: {} - Error: {}", req.username(), e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public TokenResponse login(String username, String password) {
    log.info("Login attempt for username: {}", username);

    try {
      var u = users.findByUsername(username).orElseThrow(() -> {
        log.warn("Login failed - user not found: {}", username);
        return new BadCredentialsException("auth.login.invalid");
      });

      if (!u.isEnabled()) {
        log.warn("Login failed - user disabled: {}", username);
        throw new BadCredentialsException("auth.login.invalid");
      }

      if (!encoder.matches(password, u.getPasswordHash())) {
        log.warn("Login failed - invalid password for user: {}", username);
        throw new BadCredentialsException("auth.login.invalid");
      }

      log.debug("Password validation successful for user: {}", username);
      var auths = u.getRoleEntities().stream().map(r -> new SimpleGrantedAuthority(r.getName())).toList();
      log.debug("User roles loaded: {} for user: {}", auths, username);

      String access = jwtService.createAccess(u.getUsername(), auths, "user-service", List.of("user-service"));
      String refresh = refreshTokens.issueForUser(u);

      log.info("Login successful for user: {}", username);
      return new TokenResponse(access, refresh, 900);

    } catch (Exception e) {
      log.error("Login error for username: {} - Error: {}", username, e.getMessage());
      throw e;
    }
  }

  @Override
  public TokenResponse refresh(String refreshToken) {
    log.info("Token refresh attempt initiated");

    try {
      var jwt = jwtService.decode(refreshToken);
      log.debug("Refresh token decoded successfully");

      if (!"refresh".equals(jwt.getClaims().get("typ"))) {
        log.warn("Invalid token type for refresh: {}", jwt.getClaims().get("typ"));
        throw new BadCredentialsException("jwt.refresh.required");
      }

      String username = jwt.getSubject();
      log.debug("Processing refresh for user: {}", username);

      var u = users.findByUsername(username).orElseThrow(() -> {
        log.warn("User not found during refresh: {}", username);
        return new BadCredentialsException("auth.login.invalid");
      });

      var auths = u.getRoleEntities().stream().map(r -> new SimpleGrantedAuthority(r.getName())).toList();
      String access = jwtService.createAccess(username, auths, "user-service", List.of("user-service"));
      String newRefresh = refreshTokens.rotate(refreshToken);

      log.info("Token refresh successful for user: {}", username);
      return new TokenResponse(access, newRefresh, 900);

    } catch (Exception e) {
      log.error("Token refresh failed - Error: {}", e.getMessage());
      throw e;
    }
  }
}
