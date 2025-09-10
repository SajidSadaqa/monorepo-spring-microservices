package com.example.user.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "role_entities", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoleEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false, length = 50)
  private String name;
}

