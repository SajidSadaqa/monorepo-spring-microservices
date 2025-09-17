package com.microservices.user.domain.events.fileEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; /**
 * Event fired when file is accessed/downloaded
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class FileAccessedEvent extends FileEvent {
  private String accessedBy;
  private String accessType; // DOWNLOAD, VIEW, etc.
  private String userAgent;
  private String ipAddress;

  public FileAccessedEvent(String eventId, String fileName, String originalFileName,
                           String contentType, Long fileSize, String uploadedBy,
                           String folder, String accessedBy, String accessType,
                           String userAgent, String ipAddress) {
    super(eventId, fileName, originalFileName, contentType, fileSize, uploadedBy, folder, LocalDateTime.now());
    this.accessedBy = accessedBy;
    this.accessType = accessType;
    this.userAgent = userAgent;
    this.ipAddress = ipAddress;
  }
}
