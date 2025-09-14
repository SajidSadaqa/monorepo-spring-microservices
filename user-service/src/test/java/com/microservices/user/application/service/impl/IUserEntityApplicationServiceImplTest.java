package com.microservices.user.application.service.impl;

import com.microservices.user.application.dto.UserResponse;
import com.microservices.user.application.mapper.UserMapper;
import com.microservices.user.application.util.RedisCacheService;
import com.microservices.user.domain.entity.UserEntity;
import com.microservices.user.infrastructure.persistence.UserJpaRepository;
import com.microservices.user.interfaces.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IUserEntityApplicationServiceImplTest {

  @Mock
  private UserJpaRepository users;
  @Mock
  private UserMapper mapper;
  @Mock
  private UserApplicationServiceImpl userService;
  @Mock
  private RedisCacheService redisCacheService;


  @BeforeEach
  void setUp() {
    userService = new UserApplicationServiceImpl(users, mapper,redisCacheService);
  }

  @Test
  void list_ShouldReturnPagedUsers() {
    // Given
    UserEntity userEntity1 = UserEntity.builder().id(UUID.randomUUID()).username("userEntity1").build();
    UserEntity userEntity2 = UserEntity.builder().id(UUID.randomUUID()).username("userEntity2").build();

    UserResponse dto1 = new UserResponse(userEntity1.getId(), "userEntity1", "userEntity1@test.com", true, null, null, null);
    UserResponse dto2 = new UserResponse(userEntity2.getId(), "userEntity2", "userEntity2@test.com", true, null, null, null);

    Page<UserEntity> userPage = new PageImpl<>(List.of(userEntity1, userEntity2), PageRequest.of(0, 10), 2);
    Pageable pageable = PageRequest.of(0, 10);

    when(users.findAll(pageable)).thenReturn(userPage);
    when(mapper.toDto(userEntity1)).thenReturn(dto1);
    when(mapper.toDto(userEntity2)).thenReturn(dto2);

    // When
    var result = userService.list(pageable);

    // Then
    assertThat(result.content()).hasSize(2);
    assertThat(result.page()).isEqualTo(0);
    assertThat(result.size()).isEqualTo(10);
    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.totalPages()).isEqualTo(1);
  }

  @Test
  void getById_ShouldReturnUser() {
    // Given
    UUID userId = UUID.randomUUID();
    UserEntity userEntity = UserEntity.builder()
      .id(userId)
      .username("testuser")
      .email("test@example.com")
      .enabled(true)
      .build();

    when(users.findById(userId)).thenReturn(Optional.of(userEntity));

    // When
    UserResponse result = userService.getById(userId);

    // Then
    assertThat(result.id()).isEqualTo(userId);
    assertThat(result.username()).isEqualTo("testuser");
    assertThat(result.email()).isEqualTo("test@example.com");
    assertThat(result.enabled()).isTrue();
  }

  @Test
  void getById_ShouldThrowExceptionWhenUserNotFound() {
    // Given
    UUID userId = UUID.randomUUID();
    when(users.findById(userId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.getById(userId))
      .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void getInternalById_ShouldReturnMappedUser() {
    // Given
    UUID userId = UUID.randomUUID();
    UserEntity userEntity = UserEntity.builder().id(userId).username("testuser").build();
    UserResponse dto = new UserResponse(userId, "testuser", "test@example.com", true, null, null, null);

    when(users.findById(userId)).thenReturn(Optional.of(userEntity));
    when(mapper.toDto(userEntity)).thenReturn(dto);

    // When
    UserResponse result = userService.getInternalById(userId);

    // Then
    assertThat(result).isEqualTo(dto);
  }

  @Test
  void getInternalById_ShouldThrowExceptionWhenUserNotFound() {
    // Given
    UUID userId = UUID.randomUUID();
    when(users.findById(userId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.getInternalById(userId))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessage("UserEntity not found with ID: " + userId);
  }
}
