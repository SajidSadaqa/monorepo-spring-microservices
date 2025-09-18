package com.microservices.user.application.service.fileUpload;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public interface EventDrivenFileStorageService {

  CompletableFuture<String> uploadFileAsync(MultipartFile file, String folder, String uploadedBy);

  CompletableFuture<String> uploadFileAsync(MultipartFile file, String folder, String uploadedBy, String fileKey);

  InputStream downloadFileWithTracking(String filePath, String accessedBy, String userAgent, String ipAddress);

  void deleteFileWithTracking(String fileName, String deletedBy, String reason);

  String getFileUrl(String filePath);
}
