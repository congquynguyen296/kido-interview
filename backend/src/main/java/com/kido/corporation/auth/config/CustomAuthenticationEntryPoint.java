package com.kido.corporation.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kido.corporation.auth.constant.HTTPCode;
import com.kido.corporation.auth.dto.common.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // If an attribute contains a specific HTTPCode (set by the filter), use it
        HTTPCode code = (HTTPCode) request.getAttribute("auth_error_code");
        if (code == null) {
            code = HTTPCode.UNAUTHENTICATED;
        }

        response.setStatus(code.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        ApiResponse<?> apiResponse = ApiResponse.error(code);
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
