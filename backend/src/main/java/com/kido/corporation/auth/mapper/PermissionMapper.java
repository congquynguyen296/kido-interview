package com.kido.corporation.auth.mapper;

import com.kido.corporation.auth.dto.response.admin.PermissionResponse;
import com.kido.corporation.auth.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionResponse toPermissionResponse(Permission permission) {
        if (permission == null) {
            return null;
        }

        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .resource(permission.getResource())
                .action(permission.getAction())
                .build();
    }
}
