package com.microservices.user.domain.events.fileEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; /**
 * Event fired when file is deleted
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class FileDeletedEvent extends FileEvent {
  private String deletedBy;
  private String reason;

  public FileDeletedEvent(String eventId, String fileName, String originalFileName,
                          String contentType, Long fileSize, String uploadedBy,
                          String folder, String deletedBy, String reason) {
    super(eventId, fileName, originalFileName, contentType, fileSize, uploadedBy, folder, LocalDateTime.now());
    this.deletedBy = deletedBy;
    this.reason = reason;
  }
}
