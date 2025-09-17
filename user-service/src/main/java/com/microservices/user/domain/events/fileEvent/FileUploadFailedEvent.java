package com.microservices.user.domain.events.fileEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; /**
 * Event fired when file upload fails
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class FileUploadFailedEvent extends FileEvent {
  private String uploadId;
  private String errorMessage;
  private String errorCode;

  public FileUploadFailedEvent(String eventId, String fileName, String originalFileName,
                               String contentType, Long fileSize, String uploadedBy,
                               String folder, String uploadId, String errorMessage, String errorCode) {
    super(eventId, fileName, originalFileName, contentType, fileSize, uploadedBy, folder, LocalDateTime.now());
    this.uploadId = uploadId;
    this.errorMessage = errorMessage;
    this.errorCode = errorCode;
  }
}
