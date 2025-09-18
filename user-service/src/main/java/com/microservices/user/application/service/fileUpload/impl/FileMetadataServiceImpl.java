package com.microservices.user.application.service.fileUpload.impl;

import com.microservices.user.application.service.fileUpload.FileMetadataService;
import com.microservices.user.domain.entities.FileMetadataEntity;
import com.microservices.user.infrastructure.persistence.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileMetadataServiceImpl implements FileMetadataService {

  private final FileMetadataRepository fileMetadataRepository;

  @Override
  public FileMetadataEntity save(FileMetadataEntity metadata) {
    log.info("Saving file metadata: {}", metadata.getFileName());
    return fileMetadataRepository.save(metadata);
  }

  @Override
  public Optional<FileMetadataEntity> findByFileName(String fileName) {
    log.info("Fetching metadata for fileName={}", fileName);
    return fileMetadataRepository.findByFileNameAndIsActiveTrue(fileName);
  }

  @Override
  public void deactivateFile(String fileName) {
    fileMetadataRepository.findByFileNameAndIsActiveTrue(fileName)
      .ifPresent(meta -> {
        meta.setIsActive(false);
        fileMetadataRepository.save(meta);
        log.info("Deactivated file metadata for fileName={}", fileName);
      });
  }

  @Override
  public FileMetadataEntity saveMetadata(FileMetadataEntity metadata) {
    log.info("Saving metadata for fileName={}, isActive={}", metadata.getFileName(), metadata.getIsActive());
    return fileMetadataRepository.save(metadata); // ✅ persist updates
  }
}
