package com.microservices.user.application.service.fileUpload.impl;

import com.microservices.user.application.service.fileUpload.EventDrivenFileStorageService;
import com.microservices.user.application.service.fileUpload.FileEventPublisher;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventDrivenFileStorageServiceImpl implements EventDrivenFileStorageService {

  private final MinioClient minioClient;
  private final FileEventPublisher eventPublisher;

  @Value("${minio.bucket-name}")
  private String bucketName;

  @Override
  public CompletableFuture<String> uploadFileAsync(MultipartFile file, String folder, String uploadedBy) {
    String uploadId = UUID.randomUUID().toString();
    String fileName = generateFileName(file.getOriginalFilename(), folder);

    // Publish upload initiated event
    eventPublisher.publishFileUploadInitiated(
      fileName,
      file.getOriginalFilename(),
      file.getContentType(),
      file.getSize(),
      uploadedBy,
      folder,
      uploadId,
      bucketName
    );

    return CompletableFuture.supplyAsync(() -> {
      try {
        ensureBucketExists();

        // Upload file
        minioClient.putObject(
          PutObjectArgs.builder()
            .bucket(bucketName)
            .object(fileName)
            .stream(file.getInputStream(), file.getSize(), -1)
            .contentType(file.getContentType())
            .build()
        );

        // Generate file URL
        String fileUrl = minioClient.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
            .method(Method.GET)
            .bucket(bucketName)
            .object(fileName)
            .expiry(60 * 60 * 24) // 24 hours
            .build()
        );

        // Publish upload completed event
        eventPublisher.publishFileUploadCompleted(
          fileName,
          file.getOriginalFilename(),
          file.getContentType(),
          file.getSize(),
          uploadedBy,
          folder,
          fileUrl,
          uploadId,
          bucketName + "/" + fileName
        );

        log.info("File uploaded successfully: {} (uploadId: {})", fileName, uploadId);
        return fileName;

      } catch (Exception e) {
        // Publish upload failed event
        eventPublisher.publishFileUploadFailed(
          fileName,
          file.getOriginalFilename(),
          file.getContentType(),
          file.getSize(),
          uploadedBy,
          folder,
          uploadId,
          e.getMessage(),
          "UPLOAD_ERROR"
        );

        log.error("Failed to upload file: {} (uploadId: {})", fileName, uploadId, e);
        throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
      }
    });
  }

  @Override
  public InputStream downloadFileWithTracking(String fileName, String accessedBy,
                                              String userAgent, String ipAddress) {
    try {
      InputStream inputStream = minioClient.getObject(
        GetObjectArgs.builder()
          .bucket(bucketName)
          .object(fileName)
          .build()
      );

      // Publish file accessed event
      eventPublisher.publishFileAccessed(
        fileName,
        extractOriginalName(fileName),
        "application/octet-stream",
        0L,
        "unknown",
        extractFolder(fileName),
        accessedBy,
        "DOWNLOAD",
        userAgent,
        ipAddress
      );

      return inputStream;

    } catch (Exception e) {
      log.error("Error downloading file with tracking: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to download file", e);
    }
  }

  @Override
  public void deleteFileWithTracking(String fileName, String deletedBy, String reason) {
    try {
      minioClient.removeObject(
        RemoveObjectArgs.builder()
          .bucket(bucketName)
          .object(fileName)
          .build()
      );

      eventPublisher.publishFileDeleted(
        fileName,
        extractOriginalName(fileName),
        "application/octet-stream",
        0L,
        "unknown",
        extractFolder(fileName),
        deletedBy,
        reason
      );

      log.info("File deleted successfully: {}", fileName);

    } catch (Exception e) {
      log.error("Error deleting file: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to delete file", e);
    }
  }

  // --- private helpers ---

  private void ensureBucketExists() {
    try {
      boolean exists = minioClient.bucketExists(
        BucketExistsArgs.builder()
          .bucket(bucketName)
          .build()
      );

      if (!exists) {
        minioClient.makeBucket(
          MakeBucketArgs.builder()
            .bucket(bucketName)
            .build()
        );
        log.info("Created bucket: {}", bucketName);
      }
    } catch (Exception e) {
      log.error("Error checking/creating bucket: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to ensure bucket exists", e);
    }
  }

  private String generateFileName(String originalFileName, String folder) {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String uniqueId = UUID.randomUUID().toString().substring(0, 8);
    String extension = getFileExtension(originalFileName);

    return String.format("%s/%s_%s_%s%s",
      folder != null ? folder : "uploads",
      timestamp,
      uniqueId,
      sanitizeFileName(getFileNameWithoutExtension(originalFileName)),
      extension);
  }

  private String getFileExtension(String fileName) {
    if (fileName != null && fileName.contains(".")) {
      return fileName.substring(fileName.lastIndexOf("."));
    }
    return "";
  }

  private String getFileNameWithoutExtension(String fileName) {
    if (fileName != null && fileName.contains(".")) {
      return fileName.substring(0, fileName.lastIndexOf("."));
    }
    return fileName != null ? fileName : "file";
  }

  private String sanitizeFileName(String fileName) {
    return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
  }

  private String extractOriginalName(String fileName) {
    String[] parts = fileName.split("_");
    if (parts.length >= 3) {
      return String.join("_", java.util.Arrays.copyOfRange(parts, 2, parts.length));
    }
    return fileName;
  }

  private String extractFolder(String fileName) {
    if (fileName.contains("/")) {
      return fileName.substring(0, fileName.lastIndexOf("/"));
    }
    return "uploads";
  }
}
