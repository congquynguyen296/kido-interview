package com.kido.corporation.auth.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "app.cookie")
public class CookieProperties {
    @NotBlank
    private String refreshTokenName;
    private String path;
    private boolean secure;
    private String sameSite;
    private boolean httpOnly;
    private String domain;
}
