package com.microservices.user.application.service.fileUpload.impl;

import com.microservices.user.application.service.fileUpload.EventDrivenFileStorageService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventDrivenFileStorageServiceImpl implements EventDrivenFileStorageService {

  private final MinioClient minioClient;
  private final String bucketName = "uploads";

  @Override
  public CompletableFuture<String> uploadFileAsync(MultipartFile file, String folder, String uploadedBy) {
    String fileKey = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
    return uploadFileAsync(file, folder, uploadedBy, fileKey);
  }

  @Override
  public CompletableFuture<String> uploadFileAsync(MultipartFile file, String folder, String uploadedBy, String fileKey) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String objectName = folder + "/" + fileKey;
        log.info("Uploading file to MinIO: bucket={}, objectName={}", bucketName, objectName);

        minioClient.putObject(
          PutObjectArgs.builder()
            .bucket(bucketName)
            .object(objectName)
            .stream(file.getInputStream(), file.getSize(), -1)
            .contentType(file.getContentType())
            .build()
        );

        log.info("Upload completed for fileKey={} by {}", fileKey, uploadedBy);
        return objectName; // return full path (folder/filename)
      } catch (Exception e) {
        log.error("Error uploading file: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
      }
    });
  }

  @Override
  public InputStream downloadFileWithTracking(String filePath, String accessedBy, String userAgent, String ipAddress) {
    try {
      log.info("Downloading file from MinIO: bucket={}, path={} by={} ip={}", bucketName, filePath, accessedBy, ipAddress);

      return minioClient.getObject(
        GetObjectArgs.builder()
          .bucket(bucketName)
          .object(filePath)
          .build()
      );
    } catch (Exception e) {
      log.error("Error downloading file with tracking: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to download file", e);
    }
  }

  @Override
  public void deleteFileWithTracking(String filePath, String deletedBy, String reason) {
    try {
      log.info("Deleting file from MinIO: bucket={}, path={} by={} reason={}", bucketName, filePath, deletedBy, reason);

      minioClient.removeObject(
        RemoveObjectArgs.builder()
          .bucket(bucketName)
          .object(filePath)
          .build()
      );
    } catch (Exception e) {
      log.error("Error deleting file with tracking: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to delete file", e);
    }
  }

  @Override
  public String getFileUrl(String filePath) {
    try {
      log.info("Generating presigned URL for bucket={}, path={}", bucketName, filePath);

      return minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
          .method(Method.GET)
          .bucket(bucketName)
          .object(filePath)
          .expiry(60 * 60) // 1 hour expiry
          .build()
      );
    } catch (Exception e) {
      log.error("Error generating presigned URL: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to generate presigned URL", e);
    }
  }
}
