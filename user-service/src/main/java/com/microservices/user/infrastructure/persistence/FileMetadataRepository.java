package com.microservices.user.infrastructure.persistence;

import com.microservices.user.domain.entities.FileMetadataEntity; // Updated import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadataEntity, Long> { // Updated type

  Optional<FileMetadataEntity> findByFileNameAndIsActiveTrue(String fileName);

  List<FileMetadataEntity> findByUploadedByAndIsActiveTrue(String uploadedBy);

  List<FileMetadataEntity> findByFolderAndIsActiveTrue(String folder);

  boolean existsByFileNameAndIsActiveTrue(String fileName);
}
