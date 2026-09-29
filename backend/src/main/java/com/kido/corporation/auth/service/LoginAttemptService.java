package com.kido.corporation.auth.service;

public interface LoginAttemptService {

    void loginSucceeded(String key);

    void loginFailed(String key);
    
    boolean isBlocked(String key);
}
