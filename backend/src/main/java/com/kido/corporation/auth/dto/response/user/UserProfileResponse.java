package com.kido.corporation.auth.dto.response.user;

import com.kido.corporation.auth.constant.AuthProvider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileResponse {
    UUID id;
    String email;
    String username;
    String fullName;
    String phoneNumber;
    AuthProvider authProvider;
    boolean emailVerified;
    List<String> roles;
    List<String> permissions;
    Instant createdAt;
    Instant lastLoginAt;
}
