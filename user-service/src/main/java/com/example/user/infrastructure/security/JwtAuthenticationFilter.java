package com.example.user.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
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
    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring(7);
      try {
        Jwt jwt = jwtService.decode(token);
        // expiry check (decoder enforces iat/exp if configured; keep explicit check)
        if (jwt.getExpiresAt() != null && jwt.getExpiresAt().isBefore(Instant.now())) {
          throw new BadCredentialsException("jwt.expired");
        }
        Object rolesObj = jwt.getClaims().get("roleEntities");
        var authorities = new ArrayList<SimpleGrantedAuthority>();
        if (rolesObj instanceof Collection<?> col) {
          for (Object r : col) authorities.add(new SimpleGrantedAuthority(String.valueOf(r)));
        }
        var principal = new org.springframework.security.core.userdetails.User(jwt.getSubject(), "N/A", authorities);
        var authToken = new UsernamePasswordAuthenticationToken(principal, token, authorities);
        SecurityContextHolder.getContext().setAuthentication(authToken);
      } catch (BadCredentialsException e) {
        throw e;
      } catch (Exception e) {
        throw new BadCredentialsException("jwt.invalid", e);
      }
    }
    chain.doFilter(request, response);
  }
}
