package com.microservices.user.domain.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "refresh_tokens", indexes = {
  @Index(name = "ix_refresh_tokens_user", columnList = "user_id"),
  @Index(name = "ix_refresh_tokens_revoked", columnList = "revoked")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshTokenEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(nullable = false, length = 36, unique = true)
  private String jti;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private boolean revoked;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "replaced_by")
  private String replacedBy;

  @PrePersist
  void onCreate() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
