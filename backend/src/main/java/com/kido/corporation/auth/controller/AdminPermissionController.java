package com.kido.corporation.auth.controller;

import com.kido.corporation.auth.dto.common.ApiResponse;
import com.kido.corporation.auth.dto.request.admin.CreatePermissionRequest;
import com.kido.corporation.auth.dto.request.admin.UpdatePermissionRequest;
import com.kido.corporation.auth.dto.response.admin.CreatePermissionResponse;
import com.kido.corporation.auth.dto.response.admin.DeletePermissionResponse;
import com.kido.corporation.auth.dto.response.admin.PermissionResponse;
import com.kido.corporation.auth.dto.response.admin.UpdatePermissionResponse;
import com.kido.corporation.auth.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/permissions")
@RequiredArgsConstructor
public class AdminPermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ApiResponse<List<PermissionResponse>> getAllPermissions() {
        log.info("Admin: get all permissions");
        return ApiResponse.success(permissionService.getAllPermissions());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    public ApiResponse<CreatePermissionResponse> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        log.info("Admin: create permission name={}", request.getName());
        CreatePermissionResponse response = permissionService.createPermission(request);
        log.info("Admin: permission created name={}", request.getName());
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
    public ApiResponse<UpdatePermissionResponse> updatePermission(@PathVariable UUID id, @Valid @RequestBody UpdatePermissionRequest request) {
        log.info("Admin: update permission id={}", id);
        permissionService.updatePermission(id, request);
        log.info("Admin: permission updated id={}", id);
        return ApiResponse.success(new UpdatePermissionResponse());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    public ApiResponse<DeletePermissionResponse> deletePermission(@PathVariable UUID id) {
        log.info("Admin: delete permission id={}", id);
        permissionService.deletePermission(id);
        log.info("Admin: permission deleted id={}", id);
        return ApiResponse.success(new DeletePermissionResponse());
    }
}
