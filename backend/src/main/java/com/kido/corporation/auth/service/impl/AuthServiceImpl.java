package com.kido.corporation.auth.service.impl;

import com.kido.corporation.auth.config.AuthProperties;
import com.kido.corporation.auth.constant.AuthProvider;
import com.kido.corporation.auth.constant.HTTPCode;
import com.kido.corporation.auth.constant.TokenType;
import com.kido.corporation.auth.constant.EntityStatus;
import com.kido.corporation.auth.dto.request.auth.LoginRequest;
import com.kido.corporation.auth.dto.request.auth.RegisterRequest;
import com.kido.corporation.auth.dto.response.auth.LoginResponse;
import com.kido.corporation.auth.dto.response.auth.RefreshTokenResponse;
import com.kido.corporation.auth.dto.response.auth.RegisterResponse;
import com.kido.corporation.auth.entity.Role;
import com.kido.corporation.auth.entity.User;
import com.kido.corporation.auth.exception.AppException;
import com.kido.corporation.auth.mapper.AuthMapper;
import com.kido.corporation.auth.repository.RoleRepository;
import com.kido.corporation.auth.repository.UserRepository;
import com.kido.corporation.auth.service.AuthService;
import com.kido.corporation.auth.service.JwtService;
import com.kido.corporation.auth.service.LoginAttemptService;
import com.kido.corporation.auth.service.MailService;
import com.kido.corporation.auth.service.OtpService;
import com.kido.corporation.auth.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginAttemptService loginAttemptService;
    private final AuthMapper authMapper;
    private final AuthProperties authProperties;
    private final MailService mailService;
    private final OtpService otpService;
    private final com.kido.corporation.auth.config.OtpProperties otpProperties;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(HTTPCode.INVALID_REQUEST, "Passwords do not match");
        }

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new AppException(HTTPCode.EMAIL_ALREADY_EXISTS);
        }

        // Auto-derive username from email prefix if not provided
        String username = request.getUsername();
        if (username == null || username.isBlank()) {
            String emailPrefix = request.getEmail().substring(0, request.getEmail().indexOf('@'));
            // Ensure uniqueness by appending a suffix if needed
            username = emailPrefix;
            int suffix = 1;
            while (userRepository.existsByUsername(username)) {
                username = emailPrefix + suffix;
                suffix++;
            }
        } else {
            if (userRepository.existsByUsername(username)) {
                throw new AppException(HTTPCode.USERNAME_ALREADY_EXISTS);
            }
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(HTTPCode.INTERNAL_ERROR, "Default role not found"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setStatus(EntityStatus.ACTIVE);
        user.setEmailVerified(!authProperties.isRequireEmailVerification());
        user.setRoles(Set.of(userRole));

        user = userRepository.save(user);

        // Send mail
        if (authProperties.isRequireEmailVerification()) {
            String token = otpService.generateAndSaveEmailVerificationToken(user.getEmail());
            mailService.sendVerifyEmail(user.getEmail(), user.getFullName(), token);
        }
        log.info("Verification email sent to: {}, emailVerificationRequired: {}", user.getEmail(), authProperties.isRequireEmailVerification());

        return authMapper.toRegisterResponse(user, authProperties.isRequireEmailVerification());
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String key = request.getUsernameOrEmail();

        if (loginAttemptService.isBlocked(key)) {
            throw new AppException(HTTPCode.ACCOUNT_LOCKED, "Account is temporarily locked due to multiple failed login attempts.");
        }

        Optional<User> userOpt;
        if (key.contains("@")) {
            userOpt = userRepository.findByEmailIgnoreCase(key);
        } else {
            userOpt = userRepository.findByUsername(key);
        }

        if (userOpt.isEmpty()) {
            loginAttemptService.loginFailed(key);
            throw new AppException(HTTPCode.INVALID_CREDENTIALS);
        }

        User user = userOpt.get();

        if (user.getStatus() != EntityStatus.ACTIVE) {
            throw new AppException(HTTPCode.FORBIDDEN, "User account is not active");
        }

        if (!user.isEmailVerified()) {
            throw new AppException(HTTPCode.FORBIDDEN, "Email is not verified");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginAttemptService.loginFailed(key);
            throw new AppException(HTTPCode.INVALID_CREDENTIALS);
        }

        loginAttemptService.loginSucceeded(key);

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return authMapper.toLoginResponse(user, accessToken, refreshToken, jwtService.parse(accessToken, TokenType.ACCESS).getExpiration());
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AppException(HTTPCode.INVALID_REQUEST, "Refresh token is missing");
        }

        Claims claims = jwtService.parse(refreshToken, TokenType.REFRESH);
        String jti = claims.getId();
        
        if (tokenBlacklistService.isBlacklisted(jti)) {
            // Reuse detection - revoke all tokens for this user
            UUID userId = UUID.fromString(claims.getSubject());
            log.warn("Reuse of revoked refresh token detected for user {}. Invalidating all sessions.", userId);
            tokenBlacklistService.invalidateAllUserSessions(userId);
            throw new AppException(HTTPCode.TOKEN_REVOKED, "Refresh token was revoked. All sessions have been invalidated.");
        }

        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HTTPCode.UNAUTHENTICATED));

        if (user.getStatus() != EntityStatus.ACTIVE) {
            throw new AppException(HTTPCode.FORBIDDEN, "User account is not active");
        }

        // Revoke the old refresh token
        tokenBlacklistService.blacklist(jti, TokenType.REFRESH, userId, claims.getExpiration().toInstant(), "Refresh token rotated");

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        
        return authMapper.toRefreshTokenResponse(newAccessToken, newRefreshToken, jwtService.parse(newAccessToken, TokenType.ACCESS).getExpiration());
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                Claims claims = jwtService.parse(accessToken, TokenType.ACCESS);
                tokenBlacklistService.blacklist(
                        claims.getId(),
                        TokenType.ACCESS,
                        UUID.fromString(claims.getSubject()),
                        claims.getExpiration().toInstant(),
                        "User logged out"
                );
            } catch (Exception e) {
                log.warn("Invalid access token provided during logout: {}", e.getMessage());
            }
        }

        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                Claims claims = jwtService.parse(refreshToken, TokenType.REFRESH);
                tokenBlacklistService.blacklist(
                        claims.getId(),
                        TokenType.REFRESH,
                        UUID.fromString(claims.getSubject()),
                        claims.getExpiration().toInstant(),
                        "User logged out"
                );
            } catch (Exception e) {
                log.warn("Invalid refresh token provided during logout: {}", e.getMessage());
            }
        }
    }

    @Override
    public void logoutAll(String accessToken) {
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                Claims claims = jwtService.parse(accessToken, TokenType.ACCESS);
                UUID userId = UUID.fromString(claims.getSubject());
                tokenBlacklistService.invalidateAllUserSessions(userId);
            } catch (Exception e) {
                log.warn("Invalid access token provided during logoutAll: {}", e.getMessage());
            }
        }
    }

    @Override
    public void verifyEmail(com.kido.corporation.auth.dto.request.auth.VerifyEmailRequest request) {
        if (!otpService.verifyAndInvalidateEmailVerificationToken(request.getEmail(), request.getToken())) {
            throw new AppException(HTTPCode.INVALID_REQUEST, "Invalid or expired verification token");
        }
        
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new AppException(HTTPCode.INVALID_REQUEST, "User not found"));
        
        if (user.isEmailVerified()) {
            throw new AppException(HTTPCode.INVALID_REQUEST, "Email is already verified");
        }
        
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public void resendVerification(com.kido.corporation.auth.dto.request.auth.ResendVerificationRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new AppException(HTTPCode.INVALID_REQUEST, "User not found"));
        
        if (user.isEmailVerified()) {
            throw new AppException(HTTPCode.INVALID_REQUEST, "Email is already verified");
        }
        
        String token = otpService.generateAndSaveEmailVerificationToken(user.getEmail());
        mailService.sendVerifyEmail(user.getEmail(), user.getFullName(), token);
    }

    @Override
    public com.kido.corporation.auth.dto.response.auth.ForgotPasswordResponse forgotPassword(com.kido.corporation.auth.dto.request.auth.ForgotPasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new AppException(HTTPCode.INVALID_REQUEST, "User not found"));
        
        String otp = otpService.generateAndSaveOtp(user.getEmail(), "RESET_PASSWORD");
        long validMinutes = otpProperties.getTtl().toMinutes();
        
        mailService.sendOtpReset(user.getEmail(), user.getFullName(), otp, validMinutes);
        
        return com.kido.corporation.auth.dto.response.auth.ForgotPasswordResponse.builder()
                .message("Password reset OTP has been sent to your email")
                .resendAfterSeconds(otpProperties.getResendCooldown().getSeconds())
                .build();
    }

    @Override
    public com.kido.corporation.auth.dto.response.auth.VerifyResetOtpResponse verifyResetOtp(com.kido.corporation.auth.dto.request.auth.VerifyResetOtpRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new AppException(HTTPCode.INVALID_REQUEST, "User not found"));
        
        if (otpService.verifyOtp(user.getEmail(), request.getOtp(), "RESET_PASSWORD")) {
            String resetToken = otpService.generateAndSaveResetToken(user.getEmail());
            return com.kido.corporation.auth.dto.response.auth.VerifyResetOtpResponse.builder()
                    .resetToken(resetToken)
                    .expiresIn(otpProperties.getResetTokenTtl().getSeconds())
                    .build();
        }
        throw new AppException(HTTPCode.OTP_INVALID, "Invalid OTP");
    }

    @Override
    public void resetPassword(com.kido.corporation.auth.dto.request.auth.ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(HTTPCode.INVALID_REQUEST, "Passwords do not match");
        }
        
        String email = otpService.verifyAndInvalidateResetToken(request.getResetToken());
        
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AppException(HTTPCode.INVALID_REQUEST, "User not found"));
                
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(java.time.Instant.now());
        userRepository.save(user);
        
        tokenBlacklistService.invalidateAllUserSessions(user.getId());
        
        mailService.sendPasswordChanged(user.getEmail(), user.getFullName());
    }
}
