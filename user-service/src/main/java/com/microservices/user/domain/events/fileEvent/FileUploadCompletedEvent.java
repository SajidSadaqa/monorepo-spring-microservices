package com.microservices.user.domain.events.fileEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; /**
 * Event fired when file upload is completed successfully
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class FileUploadCompletedEvent extends FileEvent {
  private String fileUrl;
  private String uploadId;
  private String storageLocation;

  public FileUploadCompletedEvent(String eventId, String fileName, String originalFileName,
                                  String contentType, Long fileSize, String uploadedBy,
                                  String folder, String fileUrl, String uploadId, String storageLocation) {
    super(eventId, fileName, originalFileName, contentType, fileSize, uploadedBy, folder, LocalDateTime.now());
    this.fileUrl = fileUrl;
    this.uploadId = uploadId;
    this.storageLocation = storageLocation;
  }
}
