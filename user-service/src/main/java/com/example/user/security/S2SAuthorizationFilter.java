package com.example.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class S2SAuthorizationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  public S2SAuthorizationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().startsWith("/internal/");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws ServletException, IOException {
    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (auth == null || !auth.startsWith("Bearer ")) {
      throw new BadCredentialsException("jwt.missing");
    }
    String token = auth.substring(7);
    Jwt jwt = jwtService.decode(token);
    Object typ = jwt.getClaims().get("typ");
    if (!"s2s".equals(typ)) {
      throw new BadCredentialsException("jwt.s2s.required");
    }
    List<String> aud = jwt.getAudience();
    if (aud == null || !aud.contains("user-service")) {
      throw new BadCredentialsException("jwt.s2s.audience");
    }
    chain.doFilter(request, response);
  }
}
