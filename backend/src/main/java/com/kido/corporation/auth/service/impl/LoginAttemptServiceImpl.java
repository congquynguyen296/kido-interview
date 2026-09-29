package com.kido.corporation.auth.service.impl;

import com.kido.corporation.auth.config.AuthProperties;
import com.kido.corporation.auth.constant.RedisKey;
import com.kido.corporation.auth.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final StringRedisTemplate stringRedisTemplate;
    private final AuthProperties authProperties;

    @Override
    public void loginSucceeded(String key) {
        String failKey = String.format(RedisKey.LOGIN_FAIL, key);
        String lockKey = String.format(RedisKey.LOCK, key);
        stringRedisTemplate.delete(failKey);
        stringRedisTemplate.delete(lockKey);
    }

    @Override
    public void loginFailed(String key) {
        String failKey = String.format(RedisKey.LOGIN_FAIL, key);
        Long attempts = stringRedisTemplate.opsForValue().increment(failKey);
        
        if (attempts != null && attempts == 1) {
            stringRedisTemplate.expire(failKey, authProperties.getFailedAttemptWindow().getSeconds(), TimeUnit.SECONDS);
        }

        if (attempts != null && attempts >= authProperties.getMaxFailedLoginAttempts()) {
            String lockKey = String.format(RedisKey.LOCK, key);
            stringRedisTemplate.opsForValue().set(lockKey, "locked", authProperties.getLockDuration().getSeconds(), TimeUnit.SECONDS);
            stringRedisTemplate.delete(failKey);
        }
    }

    @Override
    public boolean isBlocked(String key) {
        String lockKey = String.format(RedisKey.LOCK, key);
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey));
    }
}
