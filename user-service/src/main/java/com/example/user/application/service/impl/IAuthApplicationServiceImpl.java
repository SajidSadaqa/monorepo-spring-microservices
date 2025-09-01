package com.example.user.application.service.impl;

import com.example.user.application.service.IRefreshTokenService;
import com.example.user.domain.model.Role;
import com.example.user.domain.model.User;
import com.example.user.application.dto.SignupReq;
import com.example.user.application.dto.TokenResponse;
import com.example.user.interfaces.exception.BusinessException;
import com.example.user.domain.repository.RoleRepository;
import com.example.user.infrastructure.persistence.UserJpaRepository;
import com.example.user.infrastructure.security.JwtService;
import com.example.user.application.service.IAuthApplicationService;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IAuthApplicationServiceImpl implements IAuthApplicationService {

  private final UserJpaRepository users;
  private final RoleRepository roles;
  private final PasswordEncoder encoder;
  private final JwtService jwtService;
  private final IRefreshTokenService refreshTokens;

  @Override
  @Transactional
  public TokenResponse signup(SignupReq req) {
    if (users.existsByUsername(req.username())) {
      throw new BusinessException("USERNAME_TAKEN", "auth.username.taken");
    }
    if (users.existsByEmail(req.email())) {
      throw new BusinessException("EMAIL_TAKEN", "auth.email.taken");
    }
    Role userRole = roles.findByName("ROLE_USER")
      .orElseGet(() -> roles.save(Role.builder().name("ROLE_USER").build()));
    User u = User.builder()
      .username(req.username())
      .email(req.email())
      .passwordHash(encoder.encode(req.password()))
      .enabled(true)
      .createdAt(Instant.now())
      .updatedAt(Instant.now())
      .roles(Set.of(userRole))
      .build();
    u = users.save(u);
    var auths = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    String access = jwtService.createAccess(u.getUsername(), auths, "user-service", List.of("user-service"));
    String refresh = refreshTokens.issueForUser(u);
    return new TokenResponse(access, refresh, 900);
  }

  @Override
  public TokenResponse login(String username, String password) {
    var u = users.findByUsername(username).orElseThrow(() -> new BadCredentialsException("auth.login.invalid"));
    if (!u.isEnabled() || !encoder.matches(password, u.getPasswordHash())) {
      throw new BadCredentialsException("auth.login.invalid");
    }
    var auths = u.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.getName())).toList();
    String access = jwtService.createAccess(u.getUsername(), auths, "user-service", List.of("user-service"));
    String refresh = refreshTokens.issueForUser(u);
    return new TokenResponse(access, refresh, 900);
  }

  @Override
  public TokenResponse refresh(String refreshToken) {
    var jwt = jwtService.decode(refreshToken);
    if (!"refresh".equals(jwt.getClaims().get("typ"))) {
      throw new BadCredentialsException("jwt.refresh.required");
    }
    String username = jwt.getSubject();
    var u = users.findByUsername(username).orElseThrow(() -> new BadCredentialsException("auth.login.invalid"));
    var auths = u.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.getName())).toList();
    String access = jwtService.createAccess(username, auths, "user-service", List.of("user-service"));
    String newRefresh = refreshTokens.rotate(refreshToken);
    return new TokenResponse(access, newRefresh, 900);
  }
}
