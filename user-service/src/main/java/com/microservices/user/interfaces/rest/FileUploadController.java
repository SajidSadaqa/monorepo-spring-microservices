package com.microservices.user.interfaces.rest;

import com.microservices.user.application.service.fileUpload.impl.FileStorageServiceImpl;
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

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Management", description = "File upload, download and management operations")
public class FileUploadController {

  private final FileStorageServiceImpl fileStorageService;

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload a file", description = "Upload a single file to storage")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> uploadFile(
    @RequestParam("file") MultipartFile file,
    @RequestParam(value = "folder", required = false, defaultValue = "documents") String folder,
    HttpServletRequest request) {

    try {
      // Validate file
      validateFile(file);

      // Upload file
      String fileName = fileStorageService.uploadFile(file, folder);

      // Get file URL
      String fileUrl = fileStorageService.getFileUrl(fileName);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "File uploaded successfully");
      response.put("fileName", fileName);
      response.put("originalName", file.getOriginalFilename());
      response.put("fileSize", file.getSize());
      response.put("contentType", file.getContentType());
      response.put("fileUrl", fileUrl);
      response.put("folder", folder);

      log.info("File uploaded successfully by user: {}", getCurrentUser(request));

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      log.error("Error uploading file: {}", e.getMessage(), e);
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file");
    }
  }

  @GetMapping("/download/{fileName:.+}")
  @Operation(summary = "Download a file", description = "Download a file by filename")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileName) {

    try {
      if (!fileStorageService.fileExists(fileName)) {
        return ResponseEntity.notFound().build();
      }

      InputStream inputStream = fileStorageService.downloadFile(fileName);

      return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + extractOriginalName(fileName) + "\"")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(new InputStreamResource(inputStream));

    } catch (Exception e) {
      log.error("Error downloading file: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @DeleteMapping("/{fileName:.+}")
  @Operation(summary = "Delete a file", description = "Delete a file by filename")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String fileName) {

    try {
      if (!fileStorageService.fileExists(fileName)) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "File not found");
      }

      fileStorageService.deleteFile(fileName);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "File deleted successfully");
      response.put("fileName", fileName);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error deleting file: {}", e.getMessage(), e);
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file");
    }
  }

  @GetMapping("/{fileName:.+}/url")
  @Operation(summary = "Get file URL", description = "Get a presigned URL for file access")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> getFileUrl(@PathVariable String fileName) {

    try {
      if (!fileStorageService.fileExists(fileName)) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "File not found");
      }

      String fileUrl = fileStorageService.getFileUrl(fileName);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("fileName", fileName);
      response.put("fileUrl", fileUrl);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error generating file URL: {}", e.getMessage(), e);
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate file URL");
    }
  }

  @GetMapping("/{fileName:.+}/exists")
  @Operation(summary = "Check if file exists", description = "Check if a file exists in storage")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> fileExists(@PathVariable String fileName) {

    boolean exists = fileStorageService.fileExists(fileName);

    Map<String, Object> response = new HashMap<>();
    response.put("fileName", fileName);
    response.put("exists", exists);

    return ResponseEntity.ok(response);
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be empty");
    }

    // Check file size (10MB limit)
    if (file.getSize() > 10 * 1024 * 1024) {
      throw new IllegalArgumentException("File size cannot exceed 10MB");
    }

    // Check file type (you can customize this)
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
    // Extract original name from generated filename
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

  private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("success", false);
    errorResponse.put("error", message);
    errorResponse.put("status", status.value());
    return ResponseEntity.status(status).body(errorResponse);
  }
}
