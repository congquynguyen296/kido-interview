package com.kido.corporation.auth.service.impl;

import com.kido.corporation.auth.config.JwtProperties;
import com.kido.corporation.auth.constant.AppConstant;
import com.kido.corporation.auth.constant.TokenType;
import com.kido.corporation.auth.entity.Role;
import com.kido.corporation.auth.entity.User;
import com.kido.corporation.auth.exception.AppException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtServiceImpl jwtService;

    private User user;

    @BeforeEach
    void setUp() {
        lenient().when(jwtProperties.getSecret()).thenReturn("thisisatestsecretkeywhichisverylongandsecureenough");
        lenient().when(jwtProperties.getIssuer()).thenReturn("test-issuer");
        lenient().when(jwtProperties.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));
        lenient().when(jwtProperties.getRefreshTokenTtl()).thenReturn(Duration.ofDays(7));
        lenient().when(jwtProperties.getClockSkew()).thenReturn(Duration.ofSeconds(30));

        user = new User();
        user.setId(UUID.randomUUID());
        Role role = new Role();
        role.setName("USER");
        user.setRoles(Set.of(role));
    }

    @Test
    void testGenerateAndParseAccessToken() {
        String token = jwtService.generateAccessToken(user);
        assertNotNull(token);

        Claims claims = jwtService.parse(token, TokenType.ACCESS);
        assertEquals(user.getId().toString(), claims.getSubject());
        assertEquals("test-issuer", claims.getIssuer());
        assertEquals(TokenType.ACCESS.name(), claims.get(AppConstant.CLAIM_TYPE, String.class));
    }

    @Test
    void testGenerateAndParseRefreshToken() {
        String token = jwtService.generateRefreshToken(user);
        assertNotNull(token);

        Claims claims = jwtService.parse(token, TokenType.REFRESH);
        assertEquals(user.getId().toString(), claims.getSubject());
        assertEquals(TokenType.REFRESH.name(), claims.get(AppConstant.CLAIM_TYPE, String.class));
    }

    @Test
    void testParseWrongType() {
        String token = jwtService.generateAccessToken(user);
        assertThrows(AppException.class, () -> jwtService.parse(token, TokenType.REFRESH));
    }
}
