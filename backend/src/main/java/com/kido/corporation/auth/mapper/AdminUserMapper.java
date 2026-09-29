package com.kido.corporation.auth.mapper;

import com.kido.corporation.auth.dto.response.admin.AdminUserDetailResponse;
import com.kido.corporation.auth.dto.response.admin.AdminUserListItemResponse;
import com.kido.corporation.auth.entity.Role;
import com.kido.corporation.auth.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdminUserMapper {

    public AdminUserListItemResponse toListItemResponse(User user) {
        if (user == null) {
            return null;
        }

        return AdminUserListItemResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .authProvider(user.getAuthProvider())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .roles(user.getRoles() != null ? user.getRoles().stream().map(Role::getName).collect(Collectors.toList()) : List.of())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public AdminUserDetailResponse toDetailResponse(User user) {
        if (user == null) {
            return null;
        }

        List<String> roles = user.getRoles() != null ? user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()) : List.of();
                
        List<String> permissions = user.getRoles() != null ? user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName())
                .distinct()
                .collect(Collectors.toList()) : List.of();

        return AdminUserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .authProvider(user.getAuthProvider())
                .providerId(user.getProviderId())
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .passwordChangedAt(user.getPasswordChangedAt())
                .roles(roles)
                .permissions(permissions)
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .createdBy(user.getCreatedBy())
                .modifiedAt(user.getModifiedAt())
                .modifiedBy(user.getModifiedBy())
                .build();
    }
}
