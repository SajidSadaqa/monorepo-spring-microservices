package com.example.user.application.service.impl;

import com.example.user.application.service.RefreshTokenService;
import com.example.user.domain.model.RefreshToken;
import com.example.user.domain.model.User;
import com.example.user.domain.repository.RefreshTokenRepository;
import com.example.user.infrastructure.persistence.UserJpaRepository;
import com.example.user.infrastructure.security.JwtService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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
  public String issueForUser(User user) {
    String jti = UUID.randomUUID().toString();
    String token = jwt.createRefreshWithJti(user.getUsername(), "user-service", jti);
    Jwt parsed = jwt.decode(token);
    tokens.save(RefreshToken.builder()
      .user(user)
      .jti(jti)
      .expiresAt(parsed.getExpiresAt())
      .revoked(false)
      .build());
    return token;
  }

  @Override
  @Transactional
  public String rotate(String currentRefreshToken) {
    Jwt parsed = jwt.decode(currentRefreshToken);
    Object typ = parsed.getClaims().get("typ");
    if (!"refresh".equals(typ)) {
      throw new BadCredentialsException("jwt.refresh.required");
    }
    String username = parsed.getSubject();
    String jti = parsed.getId();
    if (jti == null || jti.isBlank()) {
      throw new BadCredentialsException("jwt.invalid");
    }
    var user = users.findByUsername(username).orElseThrow(() -> new BadCredentialsException("auth.login.invalid"));
    var stored = tokens.findByJti(jti).orElseThrow(() -> new BadCredentialsException("refresh.invalid"));
    if (stored.isRevoked()) {
      throw new BadCredentialsException("refresh.revoked");
    }
    if (stored.getExpiresAt() != null && stored.getExpiresAt().isBefore(Instant.now())) {
      throw new BadCredentialsException("refresh.expired");
    }

    // revoke old
    String newJti = UUID.randomUUID().toString();
    stored.setRevoked(true);
    stored.setReplacedBy(newJti);
    tokens.save(stored);

    // issue new
    String newToken = jwt.createRefreshWithJti(username, "user-service", newJti);
    Jwt parsedNew = jwt.decode(newToken);
    tokens.save(RefreshToken.builder()
      .user(user)
      .jti(newJti)
      .expiresAt(parsedNew.getExpiresAt())
      .revoked(false)
      .build());

    return newToken;
  }

  @Override
  @Transactional
  public void revokeAllForUser(User user) {
    List<RefreshToken> active = tokens.findByUser_IdAndRevokedFalse(user.getId());
    for (var t : active) { t.setRevoked(true); }
    tokens.saveAll(active);
  }
}
