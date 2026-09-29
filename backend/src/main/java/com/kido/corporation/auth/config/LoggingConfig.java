package com.kido.corporation.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Configuration
public class LoggingConfig {

    /**
     * Filter ghi log HTTP request/response cho mọi API call.
     * Log dạng: [METHOD] /path -> HTTP_STATUS (Xms)
     */
    @Bean
    public OncePerRequestFilter requestLoggingFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {

                long startTime = System.currentTimeMillis();
                String method = request.getMethod();
                String uri = request.getRequestURI();
                String query = request.getQueryString();
                String clientIp = getClientIp(request);

                String fullUri = query != null ? uri + "?" + query : uri;

                // Skip logging for actuator health checks and static resources
                if (uri.startsWith("/actuator") || uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs")) {
                    filterChain.doFilter(request, response);
                    return;
                }

                log.info("--> {} {} [IP: {}]", method, fullUri, clientIp);

                try {
                    filterChain.doFilter(request, response);
                } finally {
                    long duration = System.currentTimeMillis() - startTime;
                    int status = response.getStatus();

                    if (status >= 500) {
                        log.error("<-- {} {} -> {} ({}ms)", method, fullUri, status, duration);
                    } else if (status >= 400) {
                        log.warn("<-- {} {} -> {} ({}ms)", method, fullUri, status, duration);
                    } else {
                        log.info("<-- {} {} -> {} ({}ms)", method, fullUri, status, duration);
                    }
                }
            }

            private String getClientIp(HttpServletRequest request) {
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }

            @Override
            protected boolean shouldNotFilterAsyncDispatch() {
                return false;
            }
        };
    }
}
