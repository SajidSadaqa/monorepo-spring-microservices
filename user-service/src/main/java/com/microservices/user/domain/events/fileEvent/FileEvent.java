package com.microservices.user.domain.events.fileEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Base event class for all file-related events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class FileEvent {
  private String eventId;
  private String fileName;
  private String originalFileName;
  private String contentType;
  private Long fileSize;
  private String uploadedBy;
  private String folder;
  private LocalDateTime timestamp;
}

