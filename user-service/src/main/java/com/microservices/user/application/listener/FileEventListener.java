package com.microservices.user.application.listener;

import com.microservices.user.application.service.fileUpload.FileMetadataService;
import com.microservices.user.domain.entities.FileMetadataEntity;
import com.microservices.user.domain.events.fileEvent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileEventListener {

  private final FileMetadataService fileMetadataService;
  // private final NotificationService notificationService;
  // private final AuditService auditService;

  @EventListener
  @Async
  public void handleFileUploadInitiated(FileUploadInitiatedEvent event) {
    log.info("Processing FileUploadInitiatedEvent for file: {} (uploadId: {})",
      event.getFileName(), event.getUploadId());

    try {
      // Log the upload initiation
      logFileActivity("UPLOAD_INITIATED", event.getFileName(), event.getUploadedBy(),
        "File upload initiated for: " + event.getOriginalFileName());

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

      // ✅ CREATE AND SAVE FILE METADATA TO DATABASE
      FileMetadataEntity metadata = FileMetadataEntity.builder()
        .fileName(event.getFileName())                    // ✅ Just the filename: "20250918_020058_8953e3fe_9_1__1_.pdf"
        .originalName(event.getOriginalFileName())        // Original name: "9 1 (1).pdf"
        .filePath(event.getFolder() + "/" + event.getFileName())  // ✅ Full path: "documents/20250918_020058_8953e3fe_9_1__1_.pdf"
        .folder(event.getFolder())                        // "documents"
        .contentType(event.getContentType())
        .fileSize(event.getFileSize())
        .bucket("uploads")                                // Hard-coded bucket name (or inject from config)
        .uploadedBy(event.getUploadedBy())
        .isActive(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

      FileMetadataEntity savedMetadata = fileMetadataService.save(metadata);
      log.info("File metadata saved successfully: id={}, fileName={}, filePath={}",
        savedMetadata.getId(), savedMetadata.getFileName(), savedMetadata.getFilePath());

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

      // ✅ MARK FILE AS INACTIVE IN DATABASE
      fileMetadataService.deactivateFile(event.getFileName());
      log.info("File metadata deactivated for fileName={}", event.getFileName());

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

      // ✅ UPDATE LAST ACCESSED TIMESTAMP
      fileMetadataService.findByFileName(event.getFileName())
        .ifPresent(metadata -> {
          metadata.setUpdatedAt(LocalDateTime.now());
          fileMetadataService.saveMetadata(metadata);
          log.debug("Updated last accessed time for fileName={}", event.getFileName());
        });

      // Example: Update access statistics
      // analyticsService.recordFileAccess(event);

      // Example: Check for suspicious access patterns
      // securityService.checkAccessPattern(event);

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
