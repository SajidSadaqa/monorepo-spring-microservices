package com.microservices.user.application.service.fileUpload;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public interface EventDrivenFileStorageService {

  /**
   * Upload file asynchronously with event-driven approach
   */
  CompletableFuture<String> uploadFileAsync(MultipartFile file, String folder, String uploadedBy);

  /**
   * Download file with access tracking
   */
  InputStream downloadFileWithTracking(String fileName, String accessedBy,
                                       String userAgent, String ipAddress);

  /**
   * Delete file with event tracking
   */
  void deleteFileWithTracking(String fileName, String deletedBy, String reason);
}
