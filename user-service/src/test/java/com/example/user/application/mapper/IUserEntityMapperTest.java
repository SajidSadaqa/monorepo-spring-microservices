package com.example.user.application.mapper;

import com.example.user.domain.entity.RoleEntity;
import com.example.user.domain.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IUserEntityMapperTest {

  private final IUserMapper mapper = Mappers.getMapper(IUserMapper.class);

  @Test
  void toDto_ShouldMapUserToDto() {
    // Given
    UUID userId = UUID.randomUUID();
    Instant now = Instant.now();
    RoleEntity userRoleEntity = RoleEntity.builder().name("ROLE_USER").build();
    RoleEntity adminRoleEntity = RoleEntity.builder().name("ROLE_ADMIN").build();

    UserEntity userEntity = UserEntity.builder()
      .id(userId)
      .username("testuser")
      .email("test@example.com")
      .enabled(true)
      .createdAt(now)
      .updatedAt(now)
      .roleEntities(Set.of(userRoleEntity, adminRoleEntity))
      .build();

    // When
    var dto = mapper.toDto(userEntity);

    // Then
    assertThat(dto.id()).isEqualTo(userId);
    assertThat(dto.username()).isEqualTo("testuser");
    assertThat(dto.email()).isEqualTo("test@example.com");
    assertThat(dto.enabled()).isTrue();
    assertThat(dto.createdAt()).isEqualTo(now);
    assertThat(dto.updatedAt()).isEqualTo(now);
    assertThat(dto.roles()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
  }

  //method name should be toDtoException
  @Test
  void toDto_ShouldHandleNullRoles() {
    // Given
    UserEntity userEntity = UserEntity.builder()
      .id(UUID.randomUUID())
      .username("testuser")
      .email("test@example.com")
      .enabled(true)
      .roleEntities(null)
      .build();

    // When
    var dto = mapper.toDto(userEntity);

    // Then
    assertThat(dto.roles()).isEmpty();
  }

  @Test
  void mapRoles_ShouldMapRoleNamesToStrings() {
    // Given
    RoleEntity roleEntity1 = RoleEntity.builder().name("ROLE_USER").build();
    RoleEntity roleEntity2 = RoleEntity.builder().name("ROLE_ADMIN").build();
    Set<RoleEntity> roleEntities = Set.of(roleEntity1, roleEntity2);

    // When
    Set<String> result = mapper.mapRoles(roleEntities);

    // Then
    assertThat(result).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
  }

  @Test
  void mapRoles_ShouldReturnEmptySetForNull() {
    // When
    Set<String> result = mapper.mapRoles(null);

    // Then
    assertThat(result).isEmpty();
  }
}
