package com.example.user.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  private final S2SAuthorizationFilter s2sFilter;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter, S2SAuthorizationFilter s2sFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.s2sFilter = s2sFilter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html",
          "/api/auth/login","/api/auth/signup","/api/auth/refresh").permitAll()
        .requestMatchers("/internal/**").authenticated()
        .anyRequest().authenticated())
      .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
      .addFilterAfter(s2sFilter, JwtAuthFilter.class)
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .oauth2ResourceServer(oauth2 -> oauth2.jwt());
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
