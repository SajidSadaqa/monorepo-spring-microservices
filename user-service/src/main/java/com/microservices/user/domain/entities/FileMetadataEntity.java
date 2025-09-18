package com.microservices.user.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadataEntity { // This should be your class name now

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 500)
  private String fileName;

  @Column(nullable = false)
  private String originalName;

  @Column(nullable = false)
  private String contentType;

  @Column(nullable = false)
  private Long fileSize;

  @Column(nullable = false)
  private String folder;

  @Column(nullable = false)
  private String bucket;

  @Column(nullable = false)
  private String uploadedBy;

  @Column(nullable = false, length = 600)
  private String filePath;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
  private Boolean isActive = Boolean.TRUE;
}
