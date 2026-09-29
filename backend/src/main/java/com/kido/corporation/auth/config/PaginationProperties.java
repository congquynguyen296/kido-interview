package com.kido.corporation.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.pagination")
public class PaginationProperties {
    private int defaultPageSize = 10;
    private int maxPageSize = 100;
}
