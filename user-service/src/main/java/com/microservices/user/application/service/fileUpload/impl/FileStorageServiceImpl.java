package com.microservices.user.application.service.fileUpload.impl;

import com.microservices.user.application.service.fileUpload.FileStorageService;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

  private final MinioClient minioClient;

  @Value("${minio.bucket-name}")
  private String bucketName;

  @Override
  public String uploadFile(MultipartFile file, String folder) {
    try {
      ensureBucketExists();
      String fileName = generateFileName(file.getOriginalFilename(), folder);

      minioClient.putObject(
        PutObjectArgs.builder()
          .bucket(bucketName)
          .object(fileName)
          .stream(file.getInputStream(), file.getSize(), -1)
          .contentType(file.getContentType())
          .build()
      );

      log.info("File uploaded successfully: {}", fileName);
      return fileName;

    } catch (Exception e) {
      log.error("Error uploading file: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to upload file", e);
    }
  }

  @Override
  public InputStream downloadFile(String fileName) {
    try {
      return minioClient.getObject(
        GetObjectArgs.builder()
          .bucket(bucketName)
          .object(fileName)
          .build()
      );
    } catch (Exception e) {
      log.error("Error downloading file: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to download file", e);
    }
  }

  @Override
  public void deleteFile(String fileName) {
    try {
      minioClient.removeObject(
        RemoveObjectArgs.builder()
          .bucket(bucketName)
          .object(fileName)
          .build()
      );
      log.info("File deleted successfully: {}", fileName);
    } catch (Exception e) {
      log.error("Error deleting file: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to delete file", e);
    }
  }

  @Override
  public String getFileUrl(String fileName) {
    try {
      return minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
          .method(Method.GET)
          .bucket(bucketName)
          .object(fileName)
          .expiry(60 * 60 * 24) // 24 hours
          .build()
      );
    } catch (Exception e) {
      log.error("Error generating file URL: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to generate file URL", e);
    }
  }

  @Override
  public boolean fileExists(String fileName) {
    try {
      minioClient.statObject(
        StatObjectArgs.builder()
          .bucket(bucketName)
          .object(fileName)
          .build()
      );
      return true;
    } catch (Exception e) {
      return false;
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
}
