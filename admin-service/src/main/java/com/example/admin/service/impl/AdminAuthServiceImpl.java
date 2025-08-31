package com.example.admin.service.impl;

import com.example.admin.dto.TokenResponse;
import com.example.admin.exceptions.BusinessException;
import com.example.admin.security.JwtService;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthServiceImpl implements com.example.admin.service.AdminAuthService {

  private final PasswordEncoder encoder;
  private final JwtService jwtService;
  private final MessageSource ms;
  private final String adminUser;
  private final String adminPassHash;

  public AdminAuthServiceImpl(
    PasswordEncoder encoder,
    JwtService jwtService,
    MessageSource ms,
    @Value("${admin.username:admin}") String adminUser,
    @Value("${admin.passwordHash:$2a$10$8ZrMHYQ2sJxg0vhs3d9Uuee8G9V8fo4s6Iol2t.4so3p9Qv7QwZq6}") String adminPassHash) {
    this.encoder = encoder;
    this.jwtService = jwtService;
    this.ms = ms;
    this.adminUser = adminUser;
    this.adminPassHash = adminPassHash;
  }

  @Override
  public TokenResponse login(String username, String password) {
    if (!adminUser.equals(username) || !encoder.matches(password, adminPassHash)) {
      throw new BadCredentialsException("auth.login.invalid");
    }
    var roles = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    String token = jwtService.mintAccess(username, roles, "admin-service");
    long exp = Duration.ofMinutes(15).toSeconds();
    return new TokenResponse(token, null, exp);
  }
}
