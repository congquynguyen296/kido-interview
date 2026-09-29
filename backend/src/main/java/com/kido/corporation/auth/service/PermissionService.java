package com.kido.corporation.auth.service;

import com.kido.corporation.auth.dto.request.admin.CreatePermissionRequest;
import com.kido.corporation.auth.dto.request.admin.UpdatePermissionRequest;
import com.kido.corporation.auth.dto.response.admin.CreatePermissionResponse;
import com.kido.corporation.auth.dto.response.admin.PermissionResponse;

import java.util.List;
import java.util.UUID;

public interface PermissionService {

    List<PermissionResponse> getAllPermissions();

    CreatePermissionResponse createPermission(CreatePermissionRequest request);
    
    void updatePermission(UUID id, UpdatePermissionRequest request);

    void deletePermission(UUID id);
}
