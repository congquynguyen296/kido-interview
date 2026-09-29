package com.kido.corporation.auth.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "app.oauth2")
public class OAuth2Properties {
    @NotBlank
    private String frontendSuccessRedirectUri;
    @NotBlank
    private String frontendFailureRedirectUri;
}
