package com.kido.corporation.auth.dto.response.admin;

import com.kido.corporation.auth.constant.AuthProvider;
import com.kido.corporation.auth.constant.EntityStatus;
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
public class AdminUserListItemResponse {
    UUID id;
    String email;
    String username;
    String fullName;
    AuthProvider authProvider;
    EntityStatus status;
    boolean emailVerified;
    List<String> roles;
    Instant createdAt;
}
