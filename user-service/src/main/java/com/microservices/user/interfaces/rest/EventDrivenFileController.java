package com.microservices.user.interfaces.rest;

import com.microservices.user.application.service.fileUpload.EventDrivenFileStorageService;
import com.microservices.user.application.service.fileUpload.FileMetadataService;
import com.microservices.user.domain.entities.FileMetadataEntity;
import com.microservices.user.domain.events.fileEvent.helpers.MultiFileUploadRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/files/event-driven")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Event-Driven File Management", description = "File upload, download and management operations with async/event-driven approach")
public class EventDrivenFileController {

  private final EventDrivenFileStorageService eventDrivenFileStorageService;
  private final FileMetadataService fileMetadataService;

  // ========== Async Upload ==========
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload a file asynchronously", description = "Upload a single file with event-driven async storage")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public CompletableFuture<ResponseEntity<Map<String, Object>>> uploadFileAsync(
    @RequestParam("file") MultipartFile file,
    @RequestParam(value = "folder", required = false, defaultValue = "documents") String folder,
    HttpServletRequest request) {

    try {
      validateFile(file);
      String uploadedBy = getCurrentUser(request);

      return eventDrivenFileStorageService.uploadFileAsync(file, folder, uploadedBy)
        .thenApply(fullPath -> {
          String fileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);

          // Persist metadata
          FileMetadataEntity metadata = FileMetadataEntity.builder()
            .fileName(fileName)
            .originalName(file.getOriginalFilename())
            .contentType(file.getContentType())
            .fileSize(file.getSize())
            .folder(folder)
            .bucket("uploads")
            .uploadedBy(uploadedBy)
            .filePath(fullPath)
            .isActive(true)
            .build();

          fileMetadataService.saveMetadata(metadata);

          String fileUrl = eventDrivenFileStorageService.getFileUrl(fullPath);

          Map<String, Object> response = new HashMap<>();
          response.put("success", true);
          response.put("message", "File uploaded successfully (event-driven)");
          response.put("fileName", fileName);
          response.put("originalName", file.getOriginalFilename());
          response.put("fileSize", file.getSize());
          response.put("contentType", file.getContentType());
          response.put("fileUrl", fileUrl);
          response.put("folder", folder);
          response.put("id", metadata.getId());

          return ResponseEntity.ok(response);
        })
        .exceptionally(ex -> {
          log.error("Async upload failed: {}", ex.getMessage(), ex);
          return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file");
        });

    } catch (Exception e) {
      log.error("Error initiating async upload: {}", e.getMessage(), e);
      return CompletableFuture.completedFuture(
        createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to initiate upload")
      );
    }
  }

  // ========== Async Batch Upload ==========
  @PostMapping(value = "/batch-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
    summary = "Upload multiple files asynchronously",
    description = "Upload multiple files with event-driven async processing",
    requestBody = @RequestBody(
      required = true,
      content = @Content(
        mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
        schema = @Schema(implementation = MultiFileUploadRequest.class)
      )
    )
  )
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public CompletableFuture<ResponseEntity<Map<String, Object>>> uploadMultipleFilesAsync(
    @ModelAttribute MultiFileUploadRequest request,
    HttpServletRequest httpRequest) {

    MultipartFile[] files = request.getFiles();
    String folder = (request.getFolder() == null || request.getFolder().isBlank())
      ? "documents" : request.getFolder();

    log.info("Batch upload request: {} files → {}", (files != null ? files.length : 0), folder);

    try {
      String uploadedBy = getCurrentUser(httpRequest);

      for (MultipartFile file : files) {
        validateFile(file);
      }

      List<CompletableFuture<Void>> futures = new ArrayList<>();

      for (MultipartFile file : files) {
        CompletableFuture<Void> f = eventDrivenFileStorageService
          .uploadFileAsync(file, folder, uploadedBy)
          .thenAccept(fullPath -> {
            String fileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);

            FileMetadataEntity metadata = FileMetadataEntity.builder()
              .fileName(fileName)
              .originalName(file.getOriginalFilename())
              .contentType(file.getContentType())
              .fileSize(file.getSize())
              .folder(folder)
              .bucket("uploads")
              .uploadedBy(uploadedBy)
              .filePath(fullPath)
              .isActive(true)
              .build();

            fileMetadataService.saveMetadata(metadata);
            log.info("Batch metadata saved for {}", fileName);
          });

        futures.add(f);
      }

      return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> {
          Map<String, Object> response = new HashMap<>();
          response.put("success", true);
          response.put("message", "Batch upload completed");
          response.put("totalFiles", files.length);
          response.put("folder", folder);
          response.put("uploadedBy", uploadedBy);

          return ResponseEntity.ok(response);
        })
        .exceptionally(ex -> {
          log.error("Error in batch upload: {}", ex.getMessage(), ex);
          return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload one or more files");
        });

    } catch (Exception e) {
      log.error("Batch upload initiation failed: {}", e.getMessage(), e);
      return CompletableFuture.completedFuture(
        createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Batch upload initiation failed")
      );
    }
  }

  // ========== Download ==========
  @GetMapping("/download/{fileName:.+}")
  @Operation(summary = "Download a file with tracking", description = "Download a file by filename with access event tracking")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<?> downloadFileWithTracking(
    @PathVariable String fileName,
    HttpServletRequest request) {

    try {
      String accessedBy = getCurrentUser(request);
      String userAgent = request.getHeader("User-Agent");
      String ipAddress = getClientIpAddress(request);

      FileMetadataEntity metadata = fileMetadataService.findByFileName(fileName)
        .orElseThrow(() -> new IllegalArgumentException("No active file metadata found for: " + fileName));

      InputStream inputStream = eventDrivenFileStorageService.downloadFileWithTracking(
        metadata.getFilePath(), accessedBy, userAgent, ipAddress);

      return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getOriginalName() + "\"")
        .contentType(MediaType.parseMediaType(metadata.getContentType()))
        .body(new InputStreamResource(inputStream));

    } catch (IllegalArgumentException e) {
      return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (Exception e) {
      log.error("Download error: {}", e.getMessage(), e);
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to download file");
    }
  }

  // ========== Delete ==========
  @DeleteMapping("/{fileName:.+}")
  @Operation(summary = "Delete a file with tracking", description = "Delete a file by filename with deletion event tracking")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> deleteFileWithTracking(
    @PathVariable String fileName,
    @RequestParam(value = "reason", required = false, defaultValue = "User requested deletion") String reason,
    HttpServletRequest request) {

    try {
      String deletedBy = getCurrentUser(request);

      FileMetadataEntity metadata = fileMetadataService.findByFileName(fileName)
        .orElseThrow(() -> new IllegalArgumentException("No active file metadata found for: " + fileName));

      eventDrivenFileStorageService.deleteFileWithTracking(metadata.getFilePath(), deletedBy, reason);

      metadata.setIsActive(false);
      fileMetadataService.saveMetadata(metadata);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "File deleted successfully");
      response.put("fileName", fileName);

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (Exception e) {
      log.error("Delete error: {}", e.getMessage(), e);
      return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file");
    }
  }

  // ========== Helpers ==========
  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be empty");
    }
    if (file.getSize() > 10 * 1024 * 1024) {
      throw new IllegalArgumentException("File size cannot exceed 10MB");
    }
    String contentType = file.getContentType();
    if (contentType == null || !isAllowedContentType(contentType)) {
      throw new IllegalArgumentException("File type not allowed");
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

  private String getCurrentUser(HttpServletRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      return authentication.getName();
    }
    return "anonymous";
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

  private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("success", false);
    errorResponse.put("error", message);
    errorResponse.put("status", status.value());
    return ResponseEntity.status(status).body(errorResponse);
  }
}
