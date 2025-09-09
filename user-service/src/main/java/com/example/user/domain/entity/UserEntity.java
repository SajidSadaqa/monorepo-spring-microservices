package com.example.user.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "users", indexes = {
  @Index(name="ix_users_username", columnList = "username", unique = true),
  @Index(name="ix_users_email", columnList = "email", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserEntity {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false, length = 30)
  @NotBlank(message = "{auth.username.required}")
  private String username;

  @Column(nullable = false, length = 120)
  @Email(message = "{auth.email.invalid}")
  private String email;

  @Column(nullable = false)
  private String passwordHash;

  @Column(nullable = false)
  private boolean enabled;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<RoleEntity> roleEntities = new HashSet<>();
}
