package com.example.user.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S2SAuthorizationFilterTest {

  @Mock
  private JwtService jwtService;
  @Mock
  private FilterChain filterChain;
  @Mock
  private Jwt jwt;

  private S2SAuthorizationFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    filter = new S2SAuthorizationFilter(jwtService);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @Test
  void shouldNotFilterRegularEndpoints() throws ServletException, IOException {
    request.setRequestURI("/api/users");

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldProcessS2SEndpoints() throws ServletException, IOException {
    request.setRequestURI("/s2s/test");
    request.addHeader("Authorization", "Bearer validToken");

    when(jwtService.decode("validToken")).thenReturn(jwt);
    when(jwt.getClaim("s2s")).thenReturn(true);
    when(jwt.getClaim("iss")).thenReturn("user-service"); // <-- added
    when(jwt.getAudience()).thenReturn(List.of("user-service"));

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldRejectRequestWithoutAuthorizationHeader() {
    request.setRequestURI("/s2s/test");

    assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("jwt.missing");
  }

  @Test
  void shouldRejectRequestWithoutS2SClaim() {
    request.setRequestURI("/s2s/test");
    request.addHeader("Authorization", "Bearer token");

    when(jwtService.decode("token")).thenReturn(jwt);
    when(jwt.getClaim("s2s")).thenReturn(false);
    when(jwt.getClaim("iss")).thenReturn("user-service"); // <-- added

    assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("jwt.s2s.required");
  }

  @Test
  void shouldRejectRequestWithInvalidAudience() {
    request.setRequestURI("/s2s/test");
    request.addHeader("Authorization", "Bearer token");

    when(jwtService.decode("token")).thenReturn(jwt);
    when(jwt.getClaim("s2s")).thenReturn(true);
    when(jwt.getClaim("iss")).thenReturn("user-service"); // <-- added
    when(jwt.getAudience()).thenReturn(List.of("other-service"));

    assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("jwt.s2s.audience");
  }
}
