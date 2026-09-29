package com.kido.corporation.auth.entity;

import com.kido.corporation.auth.constant.TokenType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "token_validation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidation {

    @Id
    private UUID jti;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType tokenType;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Instant expiresAt;

    @Builder.Default
    private Instant revokedAt = Instant.now();

    private String reason;
}
