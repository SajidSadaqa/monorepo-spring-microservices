package com.example.user.domain.repository;

import com.example.user.domain.entity.RoleEntity;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
  Optional<RoleEntity> findByName(String name);
}
