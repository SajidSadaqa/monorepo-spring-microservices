package com.example.user.application.service;

import com.example.user.domain.entity.UserEntity;

public interface RefreshTokenService {
  /** Issues a new refresh token for the given userEntity, persists its JTI and expiry, and returns the token. */
  String issueForUser(UserEntity userEntity);

  /** Rotates a refresh token: validates current token, revokes it, issues & persists a new token, returns it. */
  String rotate(String currentRefreshToken);

  /** Revokes all active refresh tokens for the userEntity. */
  void revokeAllForUser(UserEntity userEntity);
}
