package com.example.user.domain;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false, length = 50)
  private String name;
}
