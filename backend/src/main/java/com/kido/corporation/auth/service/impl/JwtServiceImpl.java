package com.kido.corporation.auth.service.impl;

import com.kido.corporation.auth.config.JwtProperties;
import com.kido.corporation.auth.constant.AppConstant;
import com.kido.corporation.auth.constant.HTTPCode;
import com.kido.corporation.auth.constant.TokenType;
import com.kido.corporation.auth.entity.User;
import com.kido.corporation.auth.exception.AppException;
import com.kido.corporation.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getAccessTokenTtl());
        
        List<String> roles = user.getRoles().stream()
                .map(role -> AppConstant.ROLE_PREFIX + role.getName())
                .collect(Collectors.toList());
                
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getName())
                .distinct()
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(user.getId().toString())
                .id(UUID.randomUUID().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(AppConstant.CLAIM_TYPE, TokenType.ACCESS.name())
                .claim(AppConstant.CLAIM_ROLES, roles)
                .claim(AppConstant.CLAIM_PERMISSIONS, permissions)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getRefreshTokenTtl());

        return Jwts.builder()
                .subject(user.getId().toString())
                .id(UUID.randomUUID().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(AppConstant.CLAIM_TYPE, TokenType.REFRESH.name())
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public Claims parse(String token, TokenType expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer(jwtProperties.getIssuer())
                    .clockSkewSeconds(jwtProperties.getClockSkew().getSeconds())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
                    
            String type = claims.get(AppConstant.CLAIM_TYPE, String.class);
            if (!expectedType.name().equals(type)) {
                throw new AppException(HTTPCode.TOKEN_INVALID, "Invalid token type");
            }
            
            return claims;
        } catch (ExpiredJwtException e) {
            throw new AppException(HTTPCode.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new AppException(HTTPCode.TOKEN_INVALID);
        }
    }

    @Override
    public Duration getRemainingTtl(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            return Duration.ZERO;
        }
        long diff = expiration.getTime() - System.currentTimeMillis();
        return diff > 0 ? Duration.ofMillis(diff) : Duration.ZERO;
    }
}
