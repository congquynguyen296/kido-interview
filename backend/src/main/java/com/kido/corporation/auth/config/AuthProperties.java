package com.kido.corporation.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {
    private int maxFailedLoginAttempts = 5;
    private Duration lockDuration;
    private Duration failedAttemptWindow;
    private int bcryptStrength = 10;
    private int passwordMinLength = 8;
    private boolean requireEmailVerification = false;
    private List<String> publicEndpoints;
}
