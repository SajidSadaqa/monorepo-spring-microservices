package com.microservices.user.interfaces.rest;

import com.microservices.user.application.service.fileUpload.EventDrivenFileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/files/event-driven")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Event-Driven File Management", description = "Event-driven file upload, download and management operations")
public class EventDrivenFileController {

  private final EventDrivenFileStorageService eventDrivenFileStorageService;

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload a file asynchronously", description = "Upload a single file to storage with event-driven processing")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public CompletableFuture<ResponseEntity<Map<String, Object>>> uploadFileAsync(
    @RequestParam("file") MultipartFile file,
    @RequestParam(value = "folder", required = false, defaultValue = "documents") String folder,
    HttpServletRequest request) {

    try {
      // Validate file
      validateFile(file);

      String uploadedBy = getCurrentUser(request);

      // Upload file asynchronously with event publishing
      return eventDrivenFileStorageService.uploadFileAsync(file, folder, uploadedBy)
        .thenApply(fileName -> {
          Map<String, Object> response = new HashMap<>();
          response.put("success", true);
          response.put("message", "File upload initiated successfully");
          response.put("fileName", fileName);
          response.put("originalName", file.getOriginalFilename());
          response.put("fileSize", file.getSize());
          response.put("contentType", file.getContentType());
          response.put("folder", folder);
          response.put("uploadedBy", uploadedBy);
          response.put("status", "COMPLETED");

          log.info("File uploaded successfully: {} by user: {}", fileName, uploadedBy);
          return ResponseEntity.ok(response);
        })
        .exceptionally(throwable -> {
          log.error("Error in async file upload: {}", throwable.getMessage(), throwable);

          Map<String, Object> errorResponse = new HashMap<>();
          errorResponse.put("success", false);
          errorResponse.put("error", "Failed to upload file: " + throwable.getMessage());
          errorResponse.put("status", "FAILED");

          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        });

    } catch (IllegalArgumentException e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("error", e.getMessage());
      errorResponse.put("status", "VALIDATION_FAILED");

      return CompletableFuture.completedFuture(
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
      );
    } catch (Exception e) {
      log.error("Error initiating file upload: {}", e.getMessage(), e);

      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("error", "Failed to initiate file upload");
      errorResponse.put("status", "INITIATION_FAILED");

      return CompletableFuture.completedFuture(
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
      );
    }
  }

  @PostMapping(value = "/batch-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload multiple files asynchronously", description = "Upload multiple files with event-driven processing")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public CompletableFuture<ResponseEntity<Map<String, Object>>> uploadMultipleFilesAsync(
    @RequestParam("files") MultipartFile[] files,
    @RequestParam(value = "folder", required = false, defaultValue = "documents") String folder,
    HttpServletRequest request) {

    try {
      String uploadedBy = getCurrentUser(request);

      // Validate all files first
      for (MultipartFile file : files) {
        validateFile(file);
      }

      // Create array of futures for all uploads
      CompletableFuture<String>[] uploadFutures = new CompletableFuture[files.length];

      for (int i = 0; i < files.length; i++) {
        uploadFutures[i] = eventDrivenFileStorageService.uploadFileAsync(files[i], folder, uploadedBy);
      }

      // Combine all futures
      return CompletableFuture.allOf(uploadFutures)
        .thenApply(v -> {
          Map<String, Object> response = new HashMap<>();
          response.put("success", true);
          response.put("message", "Batch file upload completed successfully");
          response.put("totalFiles", files.length);
          response.put("folder", folder);
          response.put("uploadedBy", uploadedBy);
          response.put("status", "COMPLETED");

          log.info("Batch upload completed: {} files by user: {}", files.length, uploadedBy);
          return ResponseEntity.ok(response);
        })
        .exceptionally(throwable -> {
          log.error("Error in batch file upload: {}", throwable.getMessage(), throwable);

          Map<String, Object> errorResponse = new HashMap<>();
          errorResponse.put("success", false);
          errorResponse.put("error", "Failed to upload one or more files: " + throwable.getMessage());
          errorResponse.put("status", "FAILED");

          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        });

    } catch (Exception e) {
      log.error("Error initiating batch file upload: {}", e.getMessage(), e);

      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("error", "Failed to initiate batch file upload");
      errorResponse.put("status", "INITIATION_FAILED");

      return CompletableFuture.completedFuture(
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
      );
    }
  }

  @GetMapping("/download/{fileName:.+}")
  @Operation(summary = "Download a file with tracking", description = "Download a file by filename with access event tracking")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<InputStreamResource> downloadFileWithTracking(
    @PathVariable String fileName,
    HttpServletRequest request) {

    try {
      String accessedBy = getCurrentUser(request);
      String userAgent = request.getHeader("User-Agent");
      String ipAddress = getClientIpAddress(request);

      InputStream inputStream = eventDrivenFileStorageService.downloadFileWithTracking(
        fileName, accessedBy, userAgent, ipAddress);

      return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + extractOriginalName(fileName) + "\"")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(new InputStreamResource(inputStream));

    } catch (Exception e) {
      log.error("Error downloading file with tracking: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @DeleteMapping("/{fileName:.+}")
  @Operation(summary = "Delete a file with tracking", description = "Delete a file by filename with deletion event tracking")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> deleteFileWithTracking(
    @PathVariable String fileName,
    @RequestParam(value = "reason", required = false, defaultValue = "User requested deletion") String reason,
    HttpServletRequest request) {

    try {
      String deletedBy = getCurrentUser(request);

      eventDrivenFileStorageService.deleteFileWithTracking(fileName, deletedBy, reason);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "File deleted successfully with event tracking");
      response.put("fileName", fileName);
      response.put("deletedBy", deletedBy);
      response.put("reason", reason);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error deleting file with tracking: {}", e.getMessage(), e);

      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("error", "Failed to delete file");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be empty");
    }

    // Check file size (10MB limit)
    if (file.getSize() > 10 * 1024 * 1024) {
      throw new IllegalArgumentException("File size cannot exceed 10MB");
    }

    // Check file type
    String contentType = file.getContentType();
    if (contentType == null || !isAllowedContentType(contentType)) {
      throw new IllegalArgumentException("File type not allowed. Allowed types: PDF, DOC, DOCX, XLS, XLSX, JPG, JPEG, PNG");
    }
  }

  private boolean isAllowedContentType(String contentType) {
    return contentType.equals("application/pdf") ||
      contentType.equals("application/msword") ||
      contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
      contentType.equals("application/vnd.ms-excel") ||
      contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
      contentType.equals("image/jpeg") ||
      contentType.equals("image/jpg") ||
      contentType.equals("image/png") ||
      contentType.equals("text/plain");
  }

  private String extractOriginalName(String fileName) {
    String[] parts = fileName.split("_");
    if (parts.length >= 3) {
      return String.join("_", java.util.Arrays.copyOfRange(parts, 2, parts.length));
    }
    return fileName;
  }

  private String getCurrentUser(HttpServletRequest request) {
    // Extract user from JWT token or security context
    // This depends on your authentication implementation
    return "current-user"; // Replace with actual user extraction logic
  }

  private String getClientIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }

    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }

    return request.getRemoteAddr();
  }
}
