package com.kido.corporation.auth.service.impl;

import com.kido.corporation.auth.config.JwtProperties;
import com.kido.corporation.auth.constant.RedisKey;
import com.kido.corporation.auth.constant.TokenType;
import com.kido.corporation.auth.entity.TokenValidation;
import com.kido.corporation.auth.repository.TokenValidationRepository;
import com.kido.corporation.auth.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;
    private final TokenValidationRepository tokenValidationRepository;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public void blacklist(String jti, TokenType type, UUID userId, Instant expiresAt, String reason) {
        long ttlMillis = expiresAt.toEpochMilli() - System.currentTimeMillis();
        if (ttlMillis <= 0) {
            return;
        }

        String redisKey = String.format(RedisKey.BLACKLIST, jti);
        
        // Use Redis setIfAbsent (SETNX) to prevent concurrent inserts to the database
        Boolean isNew = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "revoked", ttlMillis, TimeUnit.MILLISECONDS);

        if (Boolean.TRUE.equals(isNew)) {
            // Only the thread that successfully sets the key in Redis will write to the DB
            TokenValidation tokenValidation = TokenValidation.builder()
                    .jti(UUID.fromString(jti))
                    .tokenType(type)
                    .userId(userId)
                    .expiresAt(expiresAt)
                    .revokedAt(Instant.now())
                    .reason(reason)
                    .build();
            tokenValidationRepository.save(tokenValidation);
        } else {
            log.warn("Token {} is already blacklisted (caught by Redis lock)", jti);
        }
    }

    @Override
    public boolean isBlacklisted(String jti) {
        String redisKey = String.format(RedisKey.BLACKLIST, jti);
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey));
    }

    @Override
    public void invalidateAllUserSessions(UUID userId) {
        String redisKey = String.format(RedisKey.USER_INVALIDATED_AT, userId.toString());
        long nowMillis = System.currentTimeMillis();
        long ttlMillis = jwtProperties.getRefreshTokenTtl().toMillis();
        stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(nowMillis), ttlMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public Instant getUserInvalidatedAt(UUID userId) {
        String redisKey = String.format(RedisKey.USER_INVALIDATED_AT, userId.toString());
        String val = stringRedisTemplate.opsForValue().get(redisKey);
        if (val != null) {
            try {
                return Instant.ofEpochMilli(Long.parseLong(val));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void warmUpCache() {
        log.info("Starting Token Blacklist warm-up...");
        List<TokenValidation> validTokens = tokenValidationRepository.findByExpiresAtAfter(Instant.now());
        for (TokenValidation token : validTokens) {
            long ttlMillis = token.getExpiresAt().toEpochMilli() - System.currentTimeMillis();
            if (ttlMillis > 0) {
                String redisKey = String.format(RedisKey.BLACKLIST, token.getJti());
                stringRedisTemplate.opsForValue().set(redisKey, "revoked", ttlMillis, TimeUnit.MILLISECONDS);
            }
        }
        log.info("Token Blacklist warm-up completed. Loaded {} tokens.", validTokens.size());
    }
}
