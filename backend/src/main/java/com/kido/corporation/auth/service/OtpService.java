package com.kido.corporation.auth.service;

public interface OtpService {

    String generateAndSaveOtp(String email, String purpose);

    boolean verifyOtp(String email, String otp, String purpose);

    void checkCooldown(String email, String purpose);

    String generateAndSaveResetToken(String email);

    String verifyAndInvalidateResetToken(String token);

    String generateAndSaveEmailVerificationToken(String email);

    boolean verifyAndInvalidateEmailVerificationToken(String email, String token);
}
