package com.kido.corporation.auth.repository;

import com.kido.corporation.auth.entity.TokenValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface TokenValidationRepository extends JpaRepository<TokenValidation, UUID> {
    void deleteByExpiresAtBefore(Instant now);
    java.util.List<TokenValidation> findByExpiresAtAfter(Instant now);
}
