package com.kido.corporation.auth.dto.response.auth;

import com.kido.corporation.auth.constant.TokenType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshTokenResponse {
    String accessToken;
    
    @JsonIgnore
    String refreshToken;
    
    TokenType tokenType;
    long expiresIn;
}
