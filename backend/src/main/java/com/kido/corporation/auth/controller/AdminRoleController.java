package com.kido.corporation.auth.controller;

import com.kido.corporation.auth.dto.common.ApiResponse;
import com.kido.corporation.auth.dto.request.admin.AssignPermissionsRequest;
import com.kido.corporation.auth.dto.request.admin.CreateRoleRequest;
import com.kido.corporation.auth.dto.request.admin.UpdateRoleRequest;
import com.kido.corporation.auth.dto.response.admin.AssignPermissionsResponse;
import com.kido.corporation.auth.dto.response.admin.CreateRoleResponse;
import com.kido.corporation.auth.dto.response.admin.DeleteRoleResponse;
import com.kido.corporation.auth.dto.response.admin.RoleDetailResponse;
import com.kido.corporation.auth.dto.response.admin.RoleResponse;
import com.kido.corporation.auth.dto.response.admin.UpdateRoleResponse;
import com.kido.corporation.auth.service.RoleService;
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
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<List<RoleResponse>> getAllRoles() {
        log.info("Admin: get all roles");
        return ApiResponse.success(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<RoleDetailResponse> getRoleDetail(@PathVariable UUID id) {
        log.info("Admin: get role detail id={}", id);
        return ApiResponse.success(roleService.getRoleDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ApiResponse<CreateRoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.info("Admin: create role name={}", request.getName());
        CreateRoleResponse response = roleService.createRole(request);
        log.info("Admin: role created name={}", request.getName());
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ApiResponse<UpdateRoleResponse> updateRole(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
        log.info("Admin: update role id={}", id);
        roleService.updateRole(id, request);
        log.info("Admin: role updated id={}", id);
        return ApiResponse.success(new UpdateRoleResponse());
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ApiResponse<AssignPermissionsResponse> assignPermissions(@PathVariable UUID id, @Valid @RequestBody AssignPermissionsRequest request) {
        log.info("Admin: assign permissions to role id={}, permissions={}", id, request.getPermissionNames());
        roleService.assignPermissions(id, request);
        log.info("Admin: permissions assigned to role id={}", id);
        return ApiResponse.success(new AssignPermissionsResponse());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ApiResponse<DeleteRoleResponse> deleteRole(@PathVariable UUID id) {
        log.info("Admin: delete role id={}", id);
        roleService.deleteRole(id);
        log.info("Admin: role deleted id={}", id);
        return ApiResponse.success(new DeleteRoleResponse());
    }
}
