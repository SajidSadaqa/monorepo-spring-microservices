package com.microservices.user.application.listener;

import com.microservices.user.domain.events.fileEvent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileEventListener {

  // You can inject any services you need for processing events
  // private final NotificationService notificationService;
  // private final FileMetadataService fileMetadataService;
  // private final AuditService auditService;

  @EventListener
  @Async
  public void handleFileUploadInitiated(FileUploadInitiatedEvent event) {
    log.info("Processing FileUploadInitiatedEvent for file: {} (uploadId: {})",
      event.getFileName(), event.getUploadId());

    try {
      // Example: Log the upload initiation
      logFileActivity("UPLOAD_INITIATED", event.getFileName(), event.getUploadedBy(),
        "File upload initiated for: " + event.getOriginalFileName());

      // Example: You could save upload metadata to database
      // fileMetadataService.saveUploadInitiation(event);

      // Example: Send notification to admin for large files
      if (event.getFileSize() > 5 * 1024 * 1024) { // 5MB
        log.warn("Large file upload initiated: {} ({}MB) by {}",
          event.getOriginalFileName(),
          event.getFileSize() / (1024 * 1024),
          event.getUploadedBy());
        // notificationService.notifyLargeFileUpload(event);
      }

    } catch (Exception e) {
      log.error("Error processing FileUploadInitiatedEvent: {}", e.getMessage(), e);
    }
  }

  @EventListener
  @Async
  public void handleFileUploadCompleted(FileUploadCompletedEvent event) {
    log.info("Processing FileUploadCompletedEvent for file: {} (uploadId: {})",
      event.getFileName(), event.getUploadId());

    try {
      // Log the successful upload
      logFileActivity("UPLOAD_COMPLETED", event.getFileName(), event.getUploadedBy(),
        "File upload completed: " + event.getOriginalFileName());

      // Example: Update file metadata in database
      // fileMetadataService.saveUploadCompletion(event);

      // Example: Trigger virus scanning
      // virusScanService.scanFile(event.getFileName());

      // Example: Send success notification
      // notificationService.notifyUploadSuccess(event);

      // Example: Update user storage quota
      // userService.updateStorageUsage(event.getUploadedBy(), event.getFileSize());

    } catch (Exception e) {
      log.error("Error processing FileUploadCompletedEvent: {}", e.getMessage(), e);
    }
  }

  @EventListener
  @Async
  public void handleFileUploadFailed(FileUploadFailedEvent event) {
    log.error("Processing FileUploadFailedEvent for file: {} (uploadId: {}), error: {}",
      event.getFileName(), event.getUploadId(), event.getErrorMessage());

    try {
      // Log the failed upload
      logFileActivity("UPLOAD_FAILED", event.getFileName(), event.getUploadedBy(),
        "File upload failed: " + event.getErrorMessage());

      // Example: Save error details to database
      // fileMetadataService.saveUploadFailure(event);

      // Example: Send failure notification to user
      // notificationService.notifyUploadFailure(event);

      // Example: Clean up any partial uploads
      // cleanupService.cleanupFailedUpload(event.getUploadId());

      // Example: Alert administrators for critical errors
      if ("STORAGE_FULL".equals(event.getErrorCode())) {
        log.error("CRITICAL: Storage full error during file upload");
        // alertService.sendCriticalAlert("Storage space critically low");
      }

    } catch (Exception e) {
      log.error("Error processing FileUploadFailedEvent: {}", e.getMessage(), e);
    }
  }

  @EventListener
  @Async
  public void handleFileDeleted(FileDeletedEvent event) {
    log.info("Processing FileDeletedEvent for file: {} by {}",
      event.getFileName(), event.getDeletedBy());

    try {
      // Log the file deletion
      logFileActivity("FILE_DELETED", event.getFileName(), event.getDeletedBy(),
        "File deleted: " + event.getOriginalFileName() + ", Reason: " + event.getReason());

      // Example: Update file metadata
      // fileMetadataService.markAsDeleted(event);

      // Example: Update user storage quota
      // userService.updateStorageUsage(event.getUploadedBy(), -event.getFileSize());

      // Example: Send deletion notification
      // notificationService.notifyFileDeletion(event);

      // Example: Archive file information for audit purposes
      // auditService.archiveFileRecord(event);

    } catch (Exception e) {
      log.error("Error processing FileDeletedEvent: {}", e.getMessage(), e);
    }
  }

  @EventListener
  @Async
  public void handleFileAccessed(FileAccessedEvent event) {
    log.debug("Processing FileAccessedEvent for file: {} by {} ({})",
      event.getFileName(), event.getAccessedBy(), event.getAccessType());

    try {
      // Log the file access (you might want to limit this for performance)
      if (!"VIEW".equals(event.getAccessType())) { // Only log downloads, not views
        logFileActivity("FILE_ACCESSED", event.getFileName(), event.getAccessedBy(),
          "File accessed via " + event.getAccessType());
      }

      // Example: Update access statistics
      // analyticsService.recordFileAccess(event);

      // Example: Check for suspicious access patterns
      // securityService.checkAccessPattern(event);

      // Example: Update last accessed timestamp
      // fileMetadataService.updateLastAccessed(event.getFileName(), event.getTimestamp());

    } catch (Exception e) {
      log.error("Error processing FileAccessedEvent: {}", e.getMessage(), e);
    }
  }

  private void logFileActivity(String action, String fileName, String user, String description) {
    // This is a simple logging implementation
    // In a real application, you might want to save this to a database
    log.info("FILE_ACTIVITY | Action: {} | File: {} | User: {} | Description: {} | Timestamp: {}",
      action, fileName, user, description, java.time.LocalDateTime.now());
  }
}
