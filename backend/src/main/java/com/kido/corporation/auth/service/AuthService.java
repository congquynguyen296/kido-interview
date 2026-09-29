package com.kido.corporation.auth.service;

import com.kido.corporation.auth.dto.request.auth.LoginRequest;
import com.kido.corporation.auth.dto.request.auth.RegisterRequest;
import com.kido.corporation.auth.dto.response.auth.LoginResponse;
import com.kido.corporation.auth.dto.response.auth.RegisterResponse;
import com.kido.corporation.auth.dto.response.auth.RefreshTokenResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    RefreshTokenResponse refreshToken(String refreshToken);

    void logout(String accessToken, String refreshToken);
    
    void logoutAll(String accessToken);
    
    void verifyEmail(com.kido.corporation.auth.dto.request.auth.VerifyEmailRequest request);
    
    void resendVerification(com.kido.corporation.auth.dto.request.auth.ResendVerificationRequest request);
    
    com.kido.corporation.auth.dto.response.auth.ForgotPasswordResponse forgotPassword(com.kido.corporation.auth.dto.request.auth.ForgotPasswordRequest request);
    
    com.kido.corporation.auth.dto.response.auth.VerifyResetOtpResponse verifyResetOtp(com.kido.corporation.auth.dto.request.auth.VerifyResetOtpRequest request);
    
    void resetPassword(com.kido.corporation.auth.dto.request.auth.ResetPasswordRequest request);
}
