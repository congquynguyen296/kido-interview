package com.kido.corporation.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "app.otp")
public class OtpProperties {
    private int length = 6;
    private Duration ttl;
    private int maxAttempts;
    private Duration resendCooldown;
    private Duration resetTokenTtl;
    private Duration emailVerificationTtl;
}
