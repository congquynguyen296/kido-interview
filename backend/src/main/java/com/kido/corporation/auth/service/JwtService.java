package com.kido.corporation.auth.service;

import com.kido.corporation.auth.constant.TokenType;
import com.kido.corporation.auth.entity.User;
import io.jsonwebtoken.Claims;

import java.time.Duration;

public interface JwtService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    Claims parse(String token, TokenType expectedType);
    
    Duration getRemainingTtl(Claims claims);
}
