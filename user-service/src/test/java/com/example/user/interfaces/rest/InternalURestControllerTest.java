package com.example.user.interfaces.rest;

import com.example.user.application.dto.UserResponse;
import com.example.user.application.service.UserApplicationService;
import com.example.user.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalURestController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security filter chain in this MVC slice
class InternalURestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserApplicationService userService;

  // Satisfy JwtAuthenticationFilter constructor so context loads
  @MockBean
  private JwtService jwtService;

  @Test
  @WithMockUser
  void getInternal_ShouldReturnUser() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();
    UserResponse user = new UserResponse(
      userId,
      "testuser",
      "test@example.com",
      true,
      Instant.now(),
      Instant.now(),
      Set.of("ROLE_USER")
    );

    when(userService.getInternalById(userId)).thenReturn(user);

    // When & Then
    mockMvc.perform(get("/internal/users/{id}", userId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(userId.toString()))
      .andExpect(jsonPath("$.username").value("testuser"));
  }
}
