package com.kido.corporation.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {
    private String from;
    private String fromName;
    private String frontendResetPasswordUrl;
    private String frontendVerifyEmailUrl;
}
