package com.microservices.user.interfaces.rest;

import com.microservices.user.application.dto.PageResponse;
import com.microservices.user.application.dto.UserResponse;
import com.microservices.user.application.service.UserApplicationService;
import com.microservices.user.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRestController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security filters in this MVC slice
class UserEntityRestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserApplicationService userService;

  // Satisfy JwtAuthenticationFilter dependency so the context loads
  @MockBean
  private JwtService jwtService;

  @Test
  @WithMockUser(roles = "ADMIN")
  void list_ShouldReturnPagedUsers() throws Exception {
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
    PageResponse<UserResponse> pageResponse =
      new PageResponse<>(List.of(user), 0, 20, 1, 1);

    when(userService.list(any(Pageable.class))).thenReturn(pageResponse);

    // When & Then
    mockMvc.perform(get("/api/users")
        .param("page", "0")
        .param("size", "20")
        .param("sort", "createdAt,desc"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content").isArray())
      .andExpect(jsonPath("$.content[0].username").value("testuser"))
      .andExpect(jsonPath("$.page").value(0))
      .andExpect(jsonPath("$.size").value(20))
      .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  @WithMockUser
  void get_ShouldReturnUser() throws Exception {
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

    when(userService.getById(userId)).thenReturn(user);

    // When & Then
    mockMvc.perform(get("/api/users/{id}", userId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(userId.toString()))
      .andExpect(jsonPath("$.username").value("testuser"))
      .andExpect(jsonPath("$.email").value("test@example.com"));
  }
}
