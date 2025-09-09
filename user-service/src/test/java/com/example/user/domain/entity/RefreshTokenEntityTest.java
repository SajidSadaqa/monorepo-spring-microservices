package com.example.user.domain.entity;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenEntityTest {

  @Test
  void shouldSetCreatedAtOnPrePersist() {
    // Given
    RefreshTokenEntity token = new RefreshTokenEntity();

    // When
    token.onCreate();

    // Then
    assertThat(token.getCreatedAt()).isNotNull();
    assertThat(token.getCreatedAt()).isBefore(Instant.now().plusSeconds(1));
  }

  @Test
  void shouldNotOverrideExistingCreatedAt() {
    // Given
    Instant existingTime = Instant.parse("2023-01-01T00:00:00Z");
    RefreshTokenEntity token = RefreshTokenEntity.builder()
      .createdAt(existingTime)
      .build();

    // When
    token.onCreate();

    // Then
    assertThat(token.getCreatedAt()).isEqualTo(existingTime);
  }
}
