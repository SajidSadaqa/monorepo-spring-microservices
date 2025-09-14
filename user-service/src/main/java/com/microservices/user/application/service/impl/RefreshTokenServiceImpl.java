package com.microservices.user.application.service.impl;

import com.microservices.user.application.service.RefreshTokenService;
import com.microservices.user.domain.entity.RefreshTokenEntity;
import com.microservices.user.domain.entity.UserEntity;
import com.microservices.user.domain.repository.RefreshTokenRepository;
import com.microservices.user.infrastructure.persistence.UserJpaRepository;
import com.microservices.user.infrastructure.security.JwtService;
import com.microservices.user.application.util.RedisCacheService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RefreshTokenRepository tokens;
  private final UserJpaRepository users;
  private final JwtService jwt;
  private final RedisCacheService redisCacheService;

  @Override
  @Transactional
  public String issueForUser(UserEntity userEntity) {
    String token = jwt.createRefreshWithJti(userEntity.getUsername(), "user-service", UUID.randomUUID().toString());
    Jwt parsed = jwt.decode(token);

    RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
      .user(userEntity)
      .jti(parsed.getId())
      .expiresAt(parsed.getExpiresAt())
      .revoked(false)
      .build();

    tokens.save(tokenEntity);

    // cache refresh token ID with TTL until expiry
    if (parsed.getExpiresAt() != null) {
      long ttl = Instant.now().until(parsed.getExpiresAt(), java.time.temporal.ChronoUnit.SECONDS);
      redisCacheService.putValue("refresh:" + parsed.getId(), userEntity.getUsername(), ttl);
    }

    return token;
  }

  @Override
  @Transactional
  public String rotate(String currentRefreshToken) {
    Jwt parsed = jwt.decode(currentRefreshToken);
    String username = parsed.getSubject();

    // mark old token in cache
    redisCacheService.putValue("refresh:revoked:" + parsed.getId(), "true", 600);

    // issue new one
    return jwt.createRefreshWithJti(username, "user-service", UUID.randomUUID().toString());
  }

  @Override
  @Transactional
  public void revokeAllForUser(UserEntity userEntity) {
    List<RefreshTokenEntity> active = tokens.findByUser_IdAndRevokedFalse(userEntity.getId());
    for (var t : active) {
      redisCacheService.putValue("refresh:revoked:" + t.getJti(), "true", 600);
    }
  }
}
