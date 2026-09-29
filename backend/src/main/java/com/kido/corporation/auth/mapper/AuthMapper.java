package com.kido.corporation.auth.mapper;

import com.kido.corporation.auth.dto.response.auth.LoginResponse;
import com.kido.corporation.auth.dto.response.auth.RegisterResponse;
import com.kido.corporation.auth.dto.response.auth.RefreshTokenResponse;
import com.kido.corporation.auth.entity.User;
import com.kido.corporation.auth.constant.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class AuthMapper {

    private final UserMapper userMapper;

    public RegisterResponse toRegisterResponse(User user, boolean emailVerificationRequired) {
        if (user == null) {
            return null;
        }

        return RegisterResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .emailVerificationRequired(emailVerificationRequired)
                .build();
    }

    public LoginResponse toLoginResponse(User user, String accessToken, String refreshToken, Date expiration) {
        if (user == null) {
            return null;
        }

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TokenType.ACCESS)
                .expiresIn(expiration.getTime())
                .user(userMapper.toProfileResponse(user))
                .build();
    }

    public RefreshTokenResponse toRefreshTokenResponse(String accessToken, String newRefreshToken, Date expiration) {
        return RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .tokenType(TokenType.ACCESS)
                .expiresIn(expiration.getTime())
                .build();
    }
}
