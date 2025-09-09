package com.example.user.domain.repository;

import com.example.user.domain.entity.RefreshTokenEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
  Optional<RefreshTokenEntity> findByJti(String jti);
  List<RefreshTokenEntity> findByUser_IdAndRevokedFalse(UUID userId);
}
