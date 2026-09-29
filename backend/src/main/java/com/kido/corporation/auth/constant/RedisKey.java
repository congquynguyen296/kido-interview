package com.kido.corporation.auth.constant;

public class RedisKey {
    public static final String BLACKLIST = "auth:blacklist:%s";
    public static final String USER_INVALIDATED_AT = "auth:user-invalidated-at:%s";
    public static final String LOGIN_FAIL = "auth:login-fail:%s";
    public static final String LOCK = "auth:lock:%s";
    public static final String OTP = "auth:otp:%s:%s";
    public static final String OTP_ATTEMPTS = "auth:otp-attempts:%s:%s";
    public static final String OTP_COOLDOWN = "auth:otp-cooldown:%s:%s";
    public static final String RESET_TOKEN = "auth:reset-token:%s";
}
