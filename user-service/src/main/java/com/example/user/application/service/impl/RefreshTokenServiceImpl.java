package com.example.user.application.service.impl;

import com.example.user.application.service.RefreshTokenService;
import com.example.user.domain.entity.RefreshTokenEntity;
import com.example.user.domain.entity.UserEntity;
import com.example.user.domain.repository.RefreshTokenRepository;
import com.example.user.infrastructure.persistence.UserJpaRepository;
import com.example.user.infrastructure.security.JwtService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RefreshTokenRepository tokens;
  private final UserJpaRepository users;
  private final JwtService jwt;

  public RefreshTokenServiceImpl(RefreshTokenRepository tokens, UserJpaRepository users, JwtService jwt) {
    this.tokens = tokens;
    this.users = users;
    this.jwt = jwt;
  }

  @Override
  @Transactional
  public String issueForUser(UserEntity userEntity) {
    log.info("Issuing new refresh token for user: {}", userEntity.getUsername());

    try {
      String jti = UUID.randomUUID().toString();
      log.debug("Generated JTI: {} for user: {}", jti, userEntity.getUsername());

      String token = jwt.createRefreshWithJti(userEntity.getUsername(), "user-service", jti);
      Jwt parsed = jwt.decode(token);

      RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
        .user(userEntity)
        .jti(jti)
        .expiresAt(parsed.getExpiresAt())
        .revoked(false)
        .build();

      tokens.save(tokenEntity);
      log.info("Refresh token issued and persisted for user: {}", userEntity.getUsername());
      return token;

    } catch (Exception e) {
      log.error("Error issuing refresh token for user {}: {}", userEntity.getUsername(), e.getMessage(), e);
      throw e;
    }
  }

  @Override
  @Transactional
  public String rotate(String currentRefreshToken) {
    log.info("Rotating refresh token");

    try {
      Jwt parsed = jwt.decode(currentRefreshToken);
      String username = parsed.getSubject();
      String jti = parsed.getId();

      log.debug("Rotating token for user: {} with JTI: {}", username, jti);

      // Validation logic with logging...
      Object typ = parsed.getClaims().get("typ");
      if (!"refresh".equals(typ)) {
        log.warn("Invalid token type for rotation: {}", typ);
        throw new BadCredentialsException("jwt.refresh.required");
      }

      if (jti == null || jti.isBlank()) {
        log.warn("Missing JTI in refresh token for user: {}", username);
        throw new BadCredentialsException("jwt.invalid");
      }

      var user = users.findByUsername(username).orElseThrow(() -> {
        log.warn("User not found during token rotation: {}", username);
        return new BadCredentialsException("auth.login.invalid");
      });

      var stored = tokens.findByJti(jti).orElseThrow(() -> {
        log.warn("Stored refresh token not found for JTI: {}", jti);
        return new BadCredentialsException("refresh.invalid");
      });

      if (stored.isRevoked()) {
        log.warn("Attempting to rotate revoked token for user: {}", username);
        throw new BadCredentialsException("refresh.revoked");
      }

      if (stored.getExpiresAt() != null && stored.getExpiresAt().isBefore(Instant.now())) {
        log.warn("Attempting to rotate expired token for user: {}", username);
        throw new BadCredentialsException("refresh.expired");
      }

      // Revoke old token
      String newJti = UUID.randomUUID().toString();
      stored.setRevoked(true);
      stored.setReplacedBy(newJti);
      tokens.save(stored);
      log.debug("Old refresh token revoked for user: {}", username);

      // Issue new token
      String newToken = jwt.createRefreshWithJti(username, "user-service", newJti);
      Jwt parsedNew = jwt.decode(newToken);

      RefreshTokenEntity newTokenEntity = RefreshTokenEntity.builder()
        .user(user)
        .jti(newJti)
        .expiresAt(parsedNew.getExpiresAt())
        .revoked(false)
        .build();

      tokens.save(newTokenEntity);
      log.info("Token rotation completed successfully for user: {}", username);
      return newToken;

    } catch (Exception e) {
      log.error("Token rotation failed: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  @Transactional
  public void revokeAllForUser(UserEntity userEntity) {
    log.info("Revoking all refresh tokens for user: {}", userEntity.getUsername());

    try {
      List<RefreshTokenEntity> active = tokens.findByUser_IdAndRevokedFalse(userEntity.getId());
      log.debug("Found {} active tokens to revoke for user: {}", active.size(), userEntity.getUsername());

      for (var t : active) {
        t.setRevoked(true);
      }
      tokens.saveAll(active);

      log.info("Successfully revoked {} tokens for user: {}", active.size(), userEntity.getUsername());
    } catch (Exception e) {
      log.error("Error revoking tokens for user {}: {}", userEntity.getUsername(), e.getMessage(), e);
      throw e;
    }
  }
}
