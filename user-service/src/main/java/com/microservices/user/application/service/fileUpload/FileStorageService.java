package com.microservices.user.application.service.fileUpload;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {

  /**
   * Upload file to Minio storage
   */
  String uploadFile(MultipartFile file, String folder);

  /**
   * Download file from Minio storage
   */
  InputStream downloadFile(String fileName);

  /**
   * Delete file from Minio storage
   */
  void deleteFile(String fileName);

  /**
   * Get file URL
   */
  String getFileUrl(String fileName);

  /**
   * Check if file exists
   */
  boolean fileExists(String fileName);
}
