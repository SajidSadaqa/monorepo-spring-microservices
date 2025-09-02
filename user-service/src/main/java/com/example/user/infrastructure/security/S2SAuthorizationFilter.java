package com.example.user.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class S2SAuthorizationFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(S2SAuthorizationFilter.class);
  private final JwtService jwtService;

  public S2SAuthorizationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();

    // Only apply S2S filter to specific admin/system endpoints
    // Regular user endpoints like /internal/users/* should NOT require S2S
    boolean requiresS2S = uri.startsWith("/s2s/") ||
      uri.startsWith("/internal/admin/") ||
      uri.startsWith("/internal/system/");

    log.debug("URI: {}, Requires S2S: {}", uri, requiresS2S);

    return !requiresS2S;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws ServletException, IOException {

    String uri = request.getRequestURI();
    log.info("S2S Filter processing: {} {}", request.getMethod(), uri);

    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (auth == null || !auth.startsWith("Bearer ")) {
      log.warn("Missing or invalid Authorization header for: {}", uri);
      throw new BadCredentialsException("jwt.missing");
    }

    try {
      Jwt jwt = jwtService.decode(auth.substring(7));
      log.debug("Decoded JWT - Subject: {}, Issuer: {}, Claims: {}",
        jwt.getSubject(), jwt.getClaim("iss"), jwt.getClaims().keySet());

      // 1) Check if this is an S2S token
      boolean s2s = Boolean.TRUE.equals(jwt.getClaim("s2s"));
      log.debug("S2S claim present: {}", s2s);

      if (!s2s) {
        log.warn("Missing s2s claim for endpoint: {}. Token subject: {}, issuer: {}",
          uri, jwt.getSubject(), jwt.getClaim("iss"));
        throw new BadCredentialsException("jwt.s2s.required");
      }

      // 2) Audience must include this service
      List<String> aud = jwt.getAudience();
      log.debug("JWT audience: {}", aud);

      if (aud == null || !aud.contains("user-service")) {
        log.warn("Invalid audience for S2S request to: {}. Audience: {}", uri, aud);
        throw new BadCredentialsException("jwt.s2s.audience");
      }

      log.info("S2S authentication successful for: {}", uri);
      chain.doFilter(request, response);

    } catch (Exception e) {
      log.error("S2S authentication failed for: {}", uri, e);
      if (e instanceof BadCredentialsException) {
        throw e;
      }
      throw new BadCredentialsException("jwt.s2s.invalid", e);
    }
  }
}
