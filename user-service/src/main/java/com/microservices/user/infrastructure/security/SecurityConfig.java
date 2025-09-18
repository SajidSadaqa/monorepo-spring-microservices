package com.microservices.user.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final S2SAuthorizationFilter s2sFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, S2SAuthorizationFilter s2sFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.s2sFilter = s2sFilter;
  }

  @Value("${security.filter-chain.authorities-claim-name:roles}")
  private String authoritiesClaimName;

  @Value("${security.filter-chain.authority-prefix:ROLE_}")
  private String authorityPrefix;

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter gac = new JwtGrantedAuthoritiesConverter();
    gac.setAuthoritiesClaimName(authoritiesClaimName); // "roles"
    gac.setAuthorityPrefix(authorityPrefix);           // "ROLE_"
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(gac);
    return converter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html",
          "/api/auth/login","/api/auth/signup","/api/auth/refresh").permitAll()

        .requestMatchers(HttpMethod.POST, "/api/files/**").hasAnyRole("USER", "ADMIN")
        .requestMatchers(HttpMethod.GET,  "/api.files/**").hasAnyRole("USER", "ADMIN")
        .requestMatchers(HttpMethod.DELETE,"/api/files/**").hasRole("ADMIN")

        .requestMatchers(HttpMethod.POST, "/api/files/event-driven/**").hasAnyRole("USER", "ADMIN")
        .requestMatchers(HttpMethod.GET,  "/api/files/event-driven/**").hasAnyRole("USER", "ADMIN")
        .requestMatchers(HttpMethod.DELETE,"/api/files/event-driven/**").hasRole("ADMIN")

        .requestMatchers("/internal/**").authenticated()
        .anyRequest().authenticated())
      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
      .addFilterAfter(s2sFilter, JwtAuthenticationFilter.class)
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
