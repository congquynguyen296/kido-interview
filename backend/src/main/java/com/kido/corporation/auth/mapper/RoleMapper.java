package com.kido.corporation.auth.mapper;

import com.kido.corporation.auth.dto.response.admin.RoleDetailResponse;
import com.kido.corporation.auth.dto.response.admin.RoleResponse;
import com.kido.corporation.auth.entity.Permission;
import com.kido.corporation.auth.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public RoleResponse toRoleResponse(Role role) {
        if (role == null) {
            return null;
        }

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .systemRole(role.isSystemRole())
                .createdAt(role.getCreatedAt())
                .permissions(role.getPermissions() != null ? role.getPermissions().stream()
                        .map(Permission::getName)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    public RoleDetailResponse toRoleDetailResponse(Role role) {
        if (role == null) {
            return null;
        }

        return RoleDetailResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .systemRole(role.isSystemRole())
                .createdAt(role.getCreatedAt())
                .createdBy(role.getCreatedBy())
                .modifiedAt(role.getModifiedAt())
                .modifiedBy(role.getModifiedBy())
                .permissions(role.getPermissions() != null ? role.getPermissions().stream()
                        .map(permissionMapper::toPermissionResponse)
                        .collect(Collectors.toList()) : null)
                .build();
    }
}
