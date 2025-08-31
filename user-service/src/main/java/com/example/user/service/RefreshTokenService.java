package com.example.user.service;

import com.example.user.domain.User;

public interface RefreshTokenService {
  /** Issues a new refresh token for the given user, persists its JTI and expiry, and returns the token. */
  String issueForUser(User user);

  /** Rotates a refresh token: validates current token, revokes it, issues & persists a new token, returns it. */
  String rotate(String currentRefreshToken);

  /** Revokes all active refresh tokens for the user. */
  void revokeAllForUser(User user);
}
