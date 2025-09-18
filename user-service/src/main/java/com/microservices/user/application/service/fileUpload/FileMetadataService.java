package com.microservices.user.application.service.fileUpload;

import com.microservices.user.domain.entities.FileMetadataEntity;

import java.util.Optional;

public interface FileMetadataService {

  FileMetadataEntity save(FileMetadataEntity metadata);

  Optional<FileMetadataEntity> findByFileName(String fileName);

  void deactivateFile(String fileName);
  FileMetadataEntity saveMetadata(FileMetadataEntity metadata);
}
