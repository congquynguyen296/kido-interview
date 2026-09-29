package com.kido.corporation.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kido.corporation.auth.dto.request.auth.LoginRequest;
import com.kido.corporation.auth.dto.request.auth.RegisterRequest;
import com.kido.corporation.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import jakarta.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class AuthFlowIT {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    private final String email = "testflow@example.com";
    private final String password = "Password123!";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        userRepository.findByEmailIgnoreCase(email).ifPresent(userRepository::delete);
    }

    @Test
    void testAuthFlow() throws Exception {
        // 1. Register
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email(email)
                .password(password)
                .confirmPassword(password)
                .fullName("Test Flow User")
                .username("testflow")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.email").value(email));

        // 2. Login
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail(email)
                .password(password)
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.accessToken").exists())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseBody).path("result").path("accessToken").asText();
        Cookie refreshTokenCookie = loginResult.getResponse().getCookie("refresh_token");

        // 3. Get /users/me
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.email").value(email));

        // 4. Refresh Token
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.accessToken").exists())
                .andReturn();

        String newAccessToken = objectMapper.readTree(refreshResult.getResponse().getContentAsString()).path("result").path("accessToken").asText();
        Cookie newRefreshTokenCookie = refreshResult.getResponse().getCookie("refresh_token");

        // 5. Verify old refresh token is revoked
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized()); // Because reuse detection revokes all, or it's invalid

        // Verify old access token is revoked? Wait, our JwtFilter doesn't check DB blacklist for every request by default unless we implemented it. 
        // Oh wait, in JwtAuthenticationFilter we DID implement checking tokenBlacklistService.isBlacklisted(jti)
        // Wait, the access token isn't blacklisted upon refresh by default, only the old refresh token is.
        // Actually, we revoked all sessions for reuse detection? Yes. So old access token will fail if iat < invalidatedAt.
        // But let's check /users/me with newAccessToken. It should work, wait... reuse detection invalidates all sessions, which means EVEN newAccessToken would fail if its iat < invalidatedAt. 
        // But the newAccessToken was created AFTER the old one, but they might be created in the same millisecond? No, reuse detection happens when we try to use the revoked old refresh token. The newAccessToken was created before reuse detection happened. So reuse detection invalidates EVERYTHING before the reuse timestamp. Thus newAccessToken is also revoked.
        
        // Anyway, let's login again to get a fresh state
        MvcResult loginResult2 = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken2 = objectMapper.readTree(loginResult2.getResponse().getContentAsString()).path("result").path("accessToken").asText();
        Cookie refreshTokenCookie2 = loginResult2.getResponse().getCookie("refresh_token");

        // 6. Logout
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken2)
                        .cookie(refreshTokenCookie2))
                .andExpect(status().isOk());

        // 7. Verify old token is rejected
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken2))
                .andExpect(status().isUnauthorized());
    }
}
