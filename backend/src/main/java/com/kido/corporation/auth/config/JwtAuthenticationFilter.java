package com.kido.corporation.auth.config;

import com.kido.corporation.auth.constant.AppConstant;
import com.kido.corporation.auth.constant.HTTPCode;
import com.kido.corporation.auth.constant.TokenType;
import com.kido.corporation.auth.exception.AppException;
import com.kido.corporation.auth.service.JwtService;
import com.kido.corporation.auth.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                Claims claims = jwtService.parse(token, TokenType.ACCESS);
                String jti = claims.getId();
                String userIdStr = claims.getSubject();
                UUID userId = UUID.fromString(userIdStr);

                // Check if blacklisted
                if (tokenBlacklistService.isBlacklisted(jti)) {
                    throw new AppException(HTTPCode.TOKEN_REVOKED);
                }

                // Check if session is invalidated (e.g. password changed)
                Instant iat = claims.getIssuedAt().toInstant();
                Instant invalidatedAt = tokenBlacklistService.getUserInvalidatedAt(userId);
                if (invalidatedAt != null && iat.isBefore(invalidatedAt)) {
                    throw new AppException(HTTPCode.TOKEN_REVOKED);
                }

                // Get authorities
                List<String> roles = claims.get(AppConstant.CLAIM_ROLES, List.class);
                List<String> permissions = claims.get(AppConstant.CLAIM_PERMISSIONS, List.class);
                List<GrantedAuthority> authorities = new ArrayList<>();
                
                if (roles != null) {
                    for (String role : roles) {
                        authorities.add(new SimpleGrantedAuthority(role));
                    }
                }
                if (permissions != null) {
                    for (String permission : permissions) {
                        authorities.add(new SimpleGrantedAuthority(permission));
                    }
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, authorities
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (AppException e) {
                log.warn("JWT Authentication failed: {}", e.getCode().getMessage());
                request.setAttribute("auth_error_code", e.getCode());
                // We don't throw the exception further to allow AuthenticationEntryPoint to handle it.
                // Or if it's public endpoint, maybe it continues without authentication.
                // Since this filter runs before UsernamePasswordAuthenticationFilter, SecurityContext is empty.
            } catch (Exception e) {
                log.error("Unexpected error during JWT authentication", e);
                request.setAttribute("auth_error_code", HTTPCode.TOKEN_INVALID);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AppConstant.AUTH_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AppConstant.BEARER_PREFIX)) {
            return bearerToken.substring(AppConstant.BEARER_PREFIX.length());
        }
        return null;
    }
}
