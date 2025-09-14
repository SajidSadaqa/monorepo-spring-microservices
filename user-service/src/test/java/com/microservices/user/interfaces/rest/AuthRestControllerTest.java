package com.microservices.user.interfaces.rest;

import com.microservices.user.application.dto.AuthReq;
import com.microservices.user.application.dto.SignupReq;
import com.microservices.user.application.dto.TokenResponse;
import com.microservices.user.application.service.AuthApplicationService;
import com.microservices.user.infrastructure.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false) // disable Spring Security filter chain for this slice
class AuthRestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AuthApplicationService authService;

  // Satisfy JwtAuthenticationFilter constructor if it is discovered in the context
  @MockBean
  private JwtService jwtService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @WithMockUser
  void signup_ShouldReturnCreatedWithTokens() throws Exception {
    // Given
    SignupReq request = new SignupReq("testuser", "Str0ngPass!", "test@example.com");
    TokenResponse response = new TokenResponse("accessToken", "refreshToken", 900);

    when(authService.signup(request)).thenReturn(response);

    // When & Then
    mockMvc.perform(post("/api/auth/signup")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(header().string("Location", "/api/users/testuser"))
      .andExpect(jsonPath("$.accessToken").value("accessToken"))
      .andExpect(jsonPath("$.refreshToken").value("refreshToken"))
      .andExpect(jsonPath("$.expiresInSeconds").value(900));
  }

  @Test
  @WithMockUser
  void login_ShouldReturnOkWithTokens() throws Exception {
    // Given
    AuthReq request = new AuthReq("testuser", "Str0ngPass!");
    TokenResponse response = new TokenResponse("accessToken", "refreshToken", 900);

    when(authService.login("testuser", "Str0ngPass!")).thenReturn(response);

    // When & Then
    mockMvc.perform(post("/api/auth/login")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.accessToken").value("accessToken"))
      .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
  }

  @Test
  @WithMockUser
  void refresh_ShouldReturnOkWithNewTokens() throws Exception {
    // Given
    String refreshToken = "refreshToken";
    TokenResponse response = new TokenResponse("newAccessToken", "newRefreshToken", 900);

    when(authService.refresh(refreshToken)).thenReturn(response);

    // When & Then
    mockMvc.perform(post("/api/auth/refresh")
        .with(csrf())
        .header("Authorization", "Bearer " + refreshToken))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
      .andExpect(jsonPath("$.refreshToken").value("newRefreshToken"));
  }

  @Test
  @WithMockUser
  void signup_ShouldReturnBadRequestForInvalidInput() throws Exception {
    // Given
    SignupReq invalidRequest = new SignupReq("ab", "weak", "invalid-email");

    // When & Then
    mockMvc.perform(post("/api/auth/signup")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
      .andExpect(status().isBadRequest());
  }
}
