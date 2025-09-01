package com.example.user.domain.repository;

import com.example.user.domain.model.RefreshToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByJti(String jti);
  List<RefreshToken> findByUser_IdAndRevokedFalse(UUID userId);
}
