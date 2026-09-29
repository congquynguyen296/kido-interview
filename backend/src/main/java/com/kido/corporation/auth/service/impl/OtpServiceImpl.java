package com.kido.corporation.auth.service.impl;

import com.kido.corporation.auth.config.OtpProperties;
import com.kido.corporation.auth.constant.HTTPCode;
import com.kido.corporation.auth.constant.RedisKey;
import com.kido.corporation.auth.exception.AppException;
import com.kido.corporation.auth.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final StringRedisTemplate stringRedisTemplate;
    private final OtpProperties otpProperties;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateAndSaveOtp(String email, String purpose) {
        checkCooldown(email, purpose);

        StringBuilder otpBuilder = new StringBuilder(otpProperties.getLength());
        for (int i = 0; i < otpProperties.getLength(); i++) {
            otpBuilder.append(secureRandom.nextInt(10));
        }
        String otp = otpBuilder.toString();
        String hash = passwordEncoder.encode(otp);

        String otpKey = String.format(RedisKey.OTP, purpose, email);
        String attemptsKey = String.format(RedisKey.OTP_ATTEMPTS, purpose, email);
        String cooldownKey = String.format(RedisKey.OTP_COOLDOWN, purpose, email);

        long ttlMillis = otpProperties.getTtl().toMillis();
        stringRedisTemplate.opsForValue().set(otpKey, hash, ttlMillis, TimeUnit.MILLISECONDS);
        stringRedisTemplate.delete(attemptsKey); // Reset attempts
        
        long cooldownMillis = otpProperties.getResendCooldown().toMillis();
        stringRedisTemplate.opsForValue().set(cooldownKey, "1", cooldownMillis, TimeUnit.MILLISECONDS);

        return otp;
    }

    @Override
    public void checkCooldown(String email, String purpose) {
        String cooldownKey = String.format(RedisKey.OTP_COOLDOWN, purpose, email);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey))) {
            throw new AppException(HTTPCode.OTP_RESEND_TOO_SOON, "Please wait before requesting a new OTP.");
        }
    }

    @Override
    public boolean verifyOtp(String email, String otp, String purpose) {
        String otpKey = String.format(RedisKey.OTP, purpose, email);
        String attemptsKey = String.format(RedisKey.OTP_ATTEMPTS, purpose, email);

        String hash = stringRedisTemplate.opsForValue().get(otpKey);
        if (hash == null) {
            throw new AppException(HTTPCode.OTP_EXPIRED, "OTP has expired or does not exist.");
        }

        Long attempts = stringRedisTemplate.opsForValue().increment(attemptsKey);
        if (attempts != null && attempts > otpProperties.getMaxAttempts()) {
            stringRedisTemplate.delete(otpKey);
            stringRedisTemplate.delete(attemptsKey);
            throw new AppException(HTTPCode.OTP_TOO_MANY_ATTEMPTS, "Too many failed attempts. Please request a new OTP.");
        }

        if (passwordEncoder.matches(otp, hash)) {
            stringRedisTemplate.delete(otpKey);
            stringRedisTemplate.delete(attemptsKey);
            return true;
        } else {
            throw new AppException(HTTPCode.OTP_INVALID, "Invalid OTP.");
        }
    }

    @Override
    public String generateAndSaveResetToken(String email) {
        String token = UUID.randomUUID().toString();
        String key = String.format(RedisKey.RESET_TOKEN, token);
        long ttlMillis = otpProperties.getResetTokenTtl().toMillis();
        stringRedisTemplate.opsForValue().set(key, email, ttlMillis, TimeUnit.MILLISECONDS);
        return token;
    }

    @Override
    public String verifyAndInvalidateResetToken(String token) {
        String key = String.format(RedisKey.RESET_TOKEN, token);
        String email = stringRedisTemplate.opsForValue().get(key);
        if (email == null) {
            throw new AppException(HTTPCode.RESET_TOKEN_INVALID, "Reset token has expired or does not exist.");
        }

        stringRedisTemplate.delete(key);
        return email;
    }

    @Override
    public String generateAndSaveEmailVerificationToken(String email) {
        String token = UUID.randomUUID().toString();
        String hash = passwordEncoder.encode(token);
        String purpose = "VERIFY_EMAIL";
        String key = String.format(RedisKey.OTP, purpose, email);
        long ttlMillis = otpProperties.getEmailVerificationTtl().toMillis();
        stringRedisTemplate.opsForValue().set(key, hash, ttlMillis, TimeUnit.MILLISECONDS);
        return token;
    }

    @Override
    public boolean verifyAndInvalidateEmailVerificationToken(String email, String token) {
        String purpose = "VERIFY_EMAIL";
        String key = String.format(RedisKey.OTP, purpose, email);
        String hash = stringRedisTemplate.opsForValue().get(key);
        if (hash == null) {
            throw new AppException(HTTPCode.OTP_EXPIRED, "Verification link has expired or does not exist.");
        }

        if (passwordEncoder.matches(token, hash)) {
            stringRedisTemplate.delete(key);
            return true;
        } else {
            throw new AppException(HTTPCode.OTP_INVALID, "Invalid verification link.");
        }
    }
}
