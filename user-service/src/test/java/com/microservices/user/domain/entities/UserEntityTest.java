package com.microservices.user.domain.entities;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

  @Test
  void shouldCreateUserWithBuilder() {
    // Given
    String username = "testuser";
    String email = "test@example.com";
    String passwordHash = "hashedPassword";
    Instant now = Instant.now();
    RoleEntity roleEntity = RoleEntity.builder().name("ROLE_USER").build();

    // When
    UserEntity userEntity = UserEntity.builder()
      .username(username)
      .email(email)
      .passwordHash(passwordHash)
      .enabled(true)
      .createdAt(now)
      .updatedAt(now)
      .roleEntities(Set.of(roleEntity))
      .build();

    // Then
    assertThat(userEntity.getUsername()).isEqualTo(username);
    assertThat(userEntity.getEmail()).isEqualTo(email);
    assertThat(userEntity.getPasswordHash()).isEqualTo(passwordHash);
    assertThat(userEntity.isEnabled()).isTrue();
    assertThat(userEntity.getCreatedAt()).isEqualTo(now);
    assertThat(userEntity.getUpdatedAt()).isEqualTo(now);
    assertThat(userEntity.getRoleEntities()).hasSize(1);
    assertThat(userEntity.getRoleEntities()).contains(roleEntity);
  }

  @Test
  void shouldInitializeEmptyRolesSet() {
    // When
    UserEntity userEntity = new UserEntity();

    // Then
    assertThat(userEntity.getRoleEntities()).isNotNull();
    assertThat(userEntity.getRoleEntities()).isEmpty();
  }
}
