package com.microservices.user.application.service.fileUpload;

public interface FileEventPublisher {

  void publishFileUploadInitiated(String fileName, String originalFileName,
                                  String contentType, Long fileSize, String uploadedBy,
                                  String folder, String uploadId, String bucket);

  void publishFileUploadCompleted(String fileName, String originalFileName,
                                  String contentType, Long fileSize, String uploadedBy,
                                  String folder, String fileUrl, String uploadId, String storageLocation);

  void publishFileUploadFailed(String fileName, String originalFileName,
                               String contentType, Long fileSize, String uploadedBy,
                               String folder, String uploadId, String errorMessage, String errorCode);

  void publishFileDeleted(String fileName, String originalFileName,
                          String contentType, Long fileSize, String uploadedBy,
                          String folder, String deletedBy, String reason);

  void publishFileAccessed(String fileName, String originalFileName,
                           String contentType, Long fileSize, String uploadedBy,
                           String folder, String accessedBy, String accessType,
                           String userAgent, String ipAddress);
}
