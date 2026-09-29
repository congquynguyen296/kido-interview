package com.kido.corporation.auth.dto.response.admin;

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
public class RoleDetailResponse {
    UUID id;
    String name;
    String description;
    boolean systemRole;
    Instant createdAt;
    String createdBy;
    Instant modifiedAt;
    String modifiedBy;
    List<PermissionResponse> permissions;
}
