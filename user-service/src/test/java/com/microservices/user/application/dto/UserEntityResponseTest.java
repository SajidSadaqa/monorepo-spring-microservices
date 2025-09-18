package com.microservices.user.application.dto;

import com.microservices.user.domain.entities.RoleEntity;
import com.microservices.user.domain.entities.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityResponseTest {

  @Test
  void fromEntity_ShouldCreateDtoFromUser() {
    // Given
    UUID userId = UUID.randomUUID();
    Instant now = Instant.now();
    RoleEntity roleEntity = RoleEntity.builder().name("ROLE_USER").build();

    UserEntity userEntity = UserEntity.builder()
      .id(userId)
      .username("testuser")
      .email("test@example.com")
      .enabled(true)
      .createdAt(now)
      .updatedAt(now)
      .roleEntities(Set.of(roleEntity))
      .build();

    // When
    UserResponse dto = UserResponse.fromEntity(userEntity);

    // Then
    assertThat(dto.id()).isEqualTo(userId);
    assertThat(dto.username()).isEqualTo("testuser");
    assertThat(dto.email()).isEqualTo("test@example.com");
    assertThat(dto.enabled()).isTrue();
    assertThat(dto.createdAt()).isEqualTo(now);
    assertThat(dto.updatedAt()).isEqualTo(now);
    assertThat(dto.roles()).isNull(); // fromEntity doesn't map roleEntities
  }
}
