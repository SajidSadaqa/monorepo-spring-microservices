package com.example.admin.application.service.impl;

import com.example.admin.application.service.IAdminAuthService;
import com.example.admin.application.dto.response.TokenResDto;
import com.example.admin.infrastructure.security.JwtTokenService;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class IAdminAuthServiceImpl implements IAdminAuthService {

  private final PasswordEncoder encoder;
  private final JwtTokenService jwtTokenService;
  private final MessageSource ms;
  private final String adminUser;
  private final String adminPassHash;

  public IAdminAuthServiceImpl(
    PasswordEncoder encoder,
    JwtTokenService jwtTokenService,
    MessageSource ms,
    @Value("${admin.username:admin}") String adminUser,
    @Value("${admin.passwordHash:$2a$10$8ZrMHYQ2sJxg0vhs3d9Uuee8G9V8fo4s6Iol2t.4so3p9Qv7QwZq6}") String adminPassHash) {
    this.encoder = encoder;
    this.jwtTokenService = jwtTokenService;
    this.ms = ms;
    this.adminUser = adminUser;
    this.adminPassHash = adminPassHash;
  }

  @Override
  public TokenResDto login(String username, String password) {
    if (!adminUser.equals(username) || !encoder.matches(password, adminPassHash)) {
      throw new BadCredentialsException("auth.login.invalid");
    }
    var roles = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    String token = jwtTokenService.mintAccess(username, roles, "admin-service");
    long exp = Duration.ofMinutes(15).toSeconds();
    return new TokenResDto(token, null, exp);
  }
}
