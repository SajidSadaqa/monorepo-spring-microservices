package com.microservices.user.application.service.fileUpload.impl;

import com.microservices.user.application.service.fileUpload.FileEventPublisher;
import com.microservices.user.domain.events.fileEvent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileEventPublisherImpl implements FileEventPublisher {

  private final ApplicationEventPublisher eventPublisher;

  @Override
  public void publishFileUploadInitiated(String fileName, String originalFileName,
                                         String contentType, Long fileSize, String uploadedBy,
                                         String folder, String uploadId, String bucket) {
    try {
      FileUploadInitiatedEvent event = new FileUploadInitiatedEvent(
        UUID.randomUUID().toString(),
        fileName, originalFileName, contentType, fileSize,
        uploadedBy, folder, uploadId, bucket
      );
      eventPublisher.publishEvent(event);
      log.info("Published FileUploadInitiatedEvent for file: {}", fileName);
    } catch (Exception e) {
      log.error("Failed to publish FileUploadInitiatedEvent for file: {}", fileName, e);
    }
  }

  @Override
  public void publishFileUploadCompleted(String fileName, String originalFileName,
                                         String contentType, Long fileSize, String uploadedBy,
                                         String folder, String fileUrl, String uploadId, String storageLocation) {
    try {
      FileUploadCompletedEvent event = new FileUploadCompletedEvent(
        UUID.randomUUID().toString(),
        fileName, originalFileName, contentType, fileSize,
        uploadedBy, folder, fileUrl, uploadId, storageLocation
      );
      eventPublisher.publishEvent(event);
      log.info("Published FileUploadCompletedEvent for file: {}", fileName);
    } catch (Exception e) {
      log.error("Failed to publish FileUploadCompletedEvent for file: {}", fileName, e);
    }
  }

  @Override
  public void publishFileUploadFailed(String fileName, String originalFileName,
                                      String contentType, Long fileSize, String uploadedBy,
                                      String folder, String uploadId, String errorMessage, String errorCode) {
    try {
      FileUploadFailedEvent event = new FileUploadFailedEvent(
        UUID.randomUUID().toString(),
        fileName, originalFileName, contentType, fileSize,
        uploadedBy, folder, uploadId, errorMessage, errorCode
      );
      eventPublisher.publishEvent(event);
      log.info("Published FileUploadFailedEvent for file: {}", fileName);
    } catch (Exception e) {
      log.error("Failed to publish FileUploadFailedEvent for file: {}", fileName, e);
    }
  }

  @Override
  public void publishFileDeleted(String fileName, String originalFileName,
                                 String contentType, Long fileSize, String uploadedBy,
                                 String folder, String deletedBy, String reason) {
    try {
      FileDeletedEvent event = new FileDeletedEvent(
        UUID.randomUUID().toString(),
        fileName, originalFileName, contentType, fileSize,
        uploadedBy, folder, deletedBy, reason
      );
      eventPublisher.publishEvent(event);
      log.info("Published FileDeletedEvent for file: {}", fileName);
    } catch (Exception e) {
      log.error("Failed to publish FileDeletedEvent for file: {}", fileName, e);
    }
  }

  @Override
  public void publishFileAccessed(String fileName, String originalFileName,
                                  String contentType, Long fileSize, String uploadedBy,
                                  String folder, String accessedBy, String accessType,
                                  String userAgent, String ipAddress) {
    try {
      FileAccessedEvent event = new FileAccessedEvent(
        UUID.randomUUID().toString(),
        fileName, originalFileName, contentType, fileSize,
        uploadedBy, folder, accessedBy, accessType, userAgent, ipAddress
      );
      eventPublisher.publishEvent(event);
      log.debug("Published FileAccessedEvent for file: {}", fileName);
    } catch (Exception e) {
      log.error("Failed to publish FileAccessedEvent for file: {}", fileName, e);
    }
  }
}
