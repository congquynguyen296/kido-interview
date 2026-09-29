package com.kido.corporation.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

import org.springframework.beans.factory.InitializingBean;

@Data
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties implements InitializingBean {
    @NotBlank
    private String secret;
    @NotBlank
    private String issuer;
    @NotNull
    private Duration accessTokenTtl;
    @NotNull
    private Duration refreshTokenTtl;
    @NotNull
    private Duration clockSkew;

    @Override
    public void afterPropertiesSet() {
        if (secret == null || secret.getBytes().length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes long");
        }
    }
}
