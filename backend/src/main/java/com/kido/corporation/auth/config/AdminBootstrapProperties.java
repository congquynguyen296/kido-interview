package com.kido.corporation.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.admin-bootstrap")
public class AdminBootstrapProperties {
    private boolean enabled;
    private String email;
    private String password;
    private String fullName;
}
