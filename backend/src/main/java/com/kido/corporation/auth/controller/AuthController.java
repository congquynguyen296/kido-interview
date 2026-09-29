package com.kido.corporation.auth.controller;

import com.kido.corporation.auth.config.CookieProperties;
import com.kido.corporation.auth.config.JwtProperties;
import com.kido.corporation.auth.constant.AppConstant;
import com.kido.corporation.auth.dto.common.ApiResponse;
import com.kido.corporation.auth.dto.request.auth.LoginRequest;
import com.kido.corporation.auth.dto.request.auth.RegisterRequest;
import com.kido.corporation.auth.dto.response.auth.LoginResponse;
import com.kido.corporation.auth.dto.response.auth.LogoutAllResponse;
import com.kido.corporation.auth.dto.response.auth.LogoutResponse;
import com.kido.corporation.auth.dto.response.auth.RefreshTokenResponse;
import com.kido.corporation.auth.dto.response.auth.RegisterResponse;
import com.kido.corporation.auth.dto.request.auth.VerifyEmailRequest;
import com.kido.corporation.auth.dto.request.auth.ResendVerificationRequest;
import com.kido.corporation.auth.dto.request.auth.ForgotPasswordRequest;
import com.kido.corporation.auth.dto.request.auth.VerifyResetOtpRequest;
import com.kido.corporation.auth.dto.request.auth.ResetPasswordRequest;
import com.kido.corporation.auth.dto.response.auth.VerifyEmailResponse;
import com.kido.corporation.auth.dto.response.auth.ResendVerificationResponse;
import com.kido.corporation.auth.dto.response.auth.ForgotPasswordResponse;
import com.kido.corporation.auth.dto.response.auth.VerifyResetOtpResponse;
import com.kido.corporation.auth.dto.response.auth.ResetPasswordResponse;
import com.kido.corporation.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for email: {}", request.getEmail());
        RegisterResponse response = authService.register(request);
        log.info("Register successful for email: {}, emailVerificationRequired: {}",
                request.getEmail(), response.isEmailVerificationRequired());
        return ApiResponse.success(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for: {}", request.getUsernameOrEmail());
        LoginResponse response = authService.login(request);

        ResponseCookie cookie = createRefreshTokenCookie(response.getRefreshToken(), jwtProperties.getRefreshTokenTtl().getSeconds());
        log.info("Login successful for: {}", request.getUsernameOrEmail());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(
            @CookieValue(name = "refresh_token", required = false) String cookieToken,
            HttpServletRequest request) {

        log.debug("Token refresh request received");
        // Find cookie by configured name
        String refreshToken = cookieToken;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieProperties.getRefreshTokenName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        RefreshTokenResponse response = authService.refreshToken(refreshToken);

        ResponseCookie cookie = createRefreshTokenCookie(response.getRefreshToken(), jwtProperties.getRefreshTokenTtl().getSeconds());
        log.debug("Token refresh successful");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponse>> logout(
            HttpServletRequest request,
            @CookieValue(name = "refresh_token", required = false) String cookieToken) {

        log.info("Logout request received");
        String accessToken = extractAccessToken(request);
        String refreshToken = cookieToken;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieProperties.getRefreshTokenName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        authService.logout(accessToken, refreshToken);
        // Clear cookie
        ResponseCookie cookie = createRefreshTokenCookie("", 0);
        log.info("Logout successful");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(new LogoutResponse()));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<LogoutAllResponse>> logoutAll(HttpServletRequest request) {
        log.info("Logout-all request received");
        String accessToken = extractAccessToken(request);
        authService.logoutAll(accessToken);

        // Clear cookie
        ResponseCookie cookie = createRefreshTokenCookie("", 0);
        log.info("Logout-all successful, all sessions invalidated");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(new LogoutAllResponse()));
    }

    @PostMapping("/verify-email")
    public ApiResponse<VerifyEmailResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        log.info("Verify email request for: {}", request.getEmail());
        authService.verifyEmail(request);
        log.info("Email verified successfully for: {}", request.getEmail());
        return ApiResponse.success(new VerifyEmailResponse());
    }

    @PostMapping("/resend-verification")
    public ApiResponse<ResendVerificationResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        log.info("Resend verification request for: {}", request.getEmail());
        authService.resendVerification(request);
        log.info("Verification email resent to: {}", request.getEmail());
        return ApiResponse.success(new ResendVerificationResponse());
    }

    @PostMapping("/forgot-password")
    public ApiResponse<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());
        ForgotPasswordResponse response = authService.forgotPassword(request);
        log.info("Forgot password processed for email: {}", request.getEmail());
        return ApiResponse.success(response);
    }

    @PostMapping("/verify-reset-otp")
    public ApiResponse<VerifyResetOtpResponse> verifyResetOtp(@Valid @RequestBody VerifyResetOtpRequest request) {
        log.info("Verify reset OTP request for email: {}", request.getEmail());
        VerifyResetOtpResponse response = authService.verifyResetOtp(request);
        log.info("OTP verified successfully for email: {}", request.getEmail());
        return ApiResponse.success(response);
    }

    @PostMapping("/reset-password")
    public ApiResponse<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Reset password request received (token present: {})", request.getResetToken() != null);
        authService.resetPassword(request);
        log.info("Password reset successful");
        return ApiResponse.success(new ResetPasswordResponse());
    }

    private ResponseCookie createRefreshTokenCookie(String token, long maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieProperties.getRefreshTokenName(), token)
                .httpOnly(cookieProperties.isHttpOnly())
                .secure(cookieProperties.isSecure())
                .path(cookieProperties.getPath())
                .maxAge(maxAge);

        if (StringUtils.hasText(cookieProperties.getDomain())) {
            builder.domain(cookieProperties.getDomain());
        }
        if (StringUtils.hasText(cookieProperties.getSameSite())) {
            builder.sameSite(cookieProperties.getSameSite());
        }

        return builder.build();
    }

    private String extractAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AppConstant.AUTH_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AppConstant.BEARER_PREFIX)) {
            return bearerToken.substring(AppConstant.BEARER_PREFIX.length());
        }
        return null;
    }
}
