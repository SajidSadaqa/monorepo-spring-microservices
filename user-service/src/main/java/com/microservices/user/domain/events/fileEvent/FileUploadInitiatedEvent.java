package com.microservices.user.domain.events.fileEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; /**
 * Event fired when file upload is initiated
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class FileUploadInitiatedEvent extends FileEvent {
  private String uploadId;
  private String bucket;

  public FileUploadInitiatedEvent(String eventId, String fileName, String originalFileName,
                                  String contentType, Long fileSize, String uploadedBy,
                                  String folder, String uploadId, String bucket) {
    super(eventId, fileName, originalFileName, contentType, fileSize, uploadedBy, folder, LocalDateTime.now());
    this.uploadId = uploadId;
    this.bucket = bucket;
  }
}
