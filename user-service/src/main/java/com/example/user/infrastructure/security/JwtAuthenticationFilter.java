package com.example.user.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final MessageSource ms;

  public JwtAuthenticationFilter(JwtService jwtService, MessageSource ms) {
    this.jwtService = jwtService;
    this.ms = ms;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws ServletException, IOException {

    String uri = request.getRequestURI();
    String method = request.getMethod();
    log.debug("Processing authentication for: {} {}", method, uri);

    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring(7);
      log.debug("Authorization header found for: {}", uri);

      try {
        Jwt jwt = jwtService.decode(token);
        log.debug("JWT decoded successfully for user: {}", jwt.getSubject());

        // Expiry check
        if (jwt.getExpiresAt() != null && jwt.getExpiresAt().isBefore(Instant.now())) {
          log.warn("Expired token used for: {} by user: {}", uri, jwt.getSubject());
          throw new BadCredentialsException("jwt.expired");
        }

        Object rolesObj = jwt.getClaims().get("roleEntities");
        var authorities = new ArrayList<SimpleGrantedAuthority>();
        if (rolesObj instanceof Collection<?> col) {
          for (Object r : col) {
            authorities.add(new SimpleGrantedAuthority(String.valueOf(r)));
          }
        }

        log.debug("Authorities extracted: {} for user: {}", authorities, jwt.getSubject());

        var principal = new org.springframework.security.core.userdetails.User(jwt.getSubject(), "N/A", authorities);
        var authToken = new UsernamePasswordAuthenticationToken(principal, token, authorities);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.debug("Authentication set successfully for user: {} accessing: {}", jwt.getSubject(), uri);

      } catch (BadCredentialsException e) {
        log.warn("Authentication failed for {}: {}", uri, e.getMessage());
        throw e;
      } catch (Exception e) {
        log.error("Unexpected authentication error for {}: {}", uri, e.getMessage(), e);
        throw new BadCredentialsException("jwt.invalid", e);
      }
    } else {
      log.debug("No authorization header found for: {}", uri);
    }

    chain.doFilter(request, response);
  }
}
