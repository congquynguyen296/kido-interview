package com.kido.corporation.auth.service;

import com.kido.corporation.auth.constant.TokenType;

import java.time.Instant;
import java.util.UUID;

public interface TokenBlacklistService {

    void blacklist(String jti, TokenType type, UUID userId, Instant expiresAt, String reason);

    boolean isBlacklisted(String jti);

    void invalidateAllUserSessions(UUID userId);
    
    Instant getUserInvalidatedAt(UUID userId);
}
