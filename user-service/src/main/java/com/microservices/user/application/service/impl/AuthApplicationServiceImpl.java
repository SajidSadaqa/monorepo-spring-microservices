package com.microservices.user.application.service.impl;

import com.microservices.user.application.service.RefreshTokenService;
import com.microservices.user.domain.entity.RoleEntity;
import com.microservices.user.domain.entity.UserEntity;
import com.microservices.user.application.dto.SignupReq;
import com.microservices.user.application.dto.TokenResponse;
import com.microservices.user.interfaces.exception.BusinessException;
import com.microservices.user.domain.repository.RoleRepository;
import com.microservices.user.infrastructure.persistence.UserJpaRepository;
import com.microservices.user.infrastructure.security.JwtService;
import com.microservices.user.application.service.AuthApplicationService;
import com.microservices.user.application.util.RedisCacheService;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final RedisCacheService redisCacheService;

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
        throw new BusinessException("EMAIL_TAKEN", "auth.username.taken");
      }

      RoleEntity userRoleEntity = roles.findByName("ROLE_USER")
        .orElseGet(() -> roles.save(RoleEntity.builder().name("ROLE_USER").build()));

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

      var auths = List.of(new SimpleGrantedAuthority("ROLE_USER"));
      String access = jwtService.createAccess(u.getUsername(), auths, "user-service", List.of("user-service"));
      String refresh = refreshTokens.issueForUser(u);

      // cache signup result briefly
      redisCacheService.putObject("user:signup:" + u.getUsername(), u, 60);

      return new TokenResponse(access, refresh, 900);
    } catch (Exception e) {
      throw e;
    }
  }

  @Override
  public TokenResponse login(String username, String password) {
    log.info("Login attempt for username: {}", username);

    try {
      var u = users.findByUsername(username).orElseThrow(() -> new BadCredentialsException("auth.login.invalid"));

      if (!u.isEnabled()) {
        throw new BadCredentialsException("auth.login.invalid");
      }

      if (!encoder.matches(password, u.getPasswordHash())) {
        throw new BadCredentialsException("auth.login.invalid");
      }

      var auths = u.getRoleEntities().stream().map(r -> new SimpleGrantedAuthority(r.getName())).toList();
      String access = jwtService.createAccess(u.getUsername(), auths, "user-service", List.of("user-service"));
      String refresh = refreshTokens.issueForUser(u);

      // cache last login time
      redisCacheService.putValue("user:lastLogin:" + u.getUsername(), Instant.now().toString(), 300);

      return new TokenResponse(access, refresh, 900);
    } catch (Exception e) {
      throw e;
    }
  }

  @Override
  public TokenResponse refresh(String refreshToken) {
    try {
      var jwt = jwtService.decode(refreshToken);
      String username = jwt.getSubject();
      var u = users.findByUsername(username).orElseThrow(() -> new BadCredentialsException("auth.login.invalid"));

      var auths = u.getRoleEntities().stream().map(r -> new SimpleGrantedAuthority(r.getName())).toList();
      String access = jwtService.createAccess(username, auths, "user-service", List.of("user-service"));
      String newRefresh = refreshTokens.rotate(refreshToken);

      // cache refresh event
      redisCacheService.putValue("user:refresh:" + username, Instant.now().toString(), 60);

      return new TokenResponse(access, newRefresh, 900);
    } catch (Exception e) {
      throw e;
    }
  }
}
