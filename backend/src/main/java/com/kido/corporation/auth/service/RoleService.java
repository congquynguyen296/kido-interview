package com.kido.corporation.auth.service;

import com.kido.corporation.auth.dto.request.admin.AssignPermissionsRequest;
import com.kido.corporation.auth.dto.request.admin.CreateRoleRequest;
import com.kido.corporation.auth.dto.request.admin.UpdateRoleRequest;
import com.kido.corporation.auth.dto.response.admin.CreateRoleResponse;
import com.kido.corporation.auth.dto.response.admin.RoleDetailResponse;
import com.kido.corporation.auth.dto.response.admin.RoleResponse;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    RoleDetailResponse getRoleDetail(UUID id);

    CreateRoleResponse createRole(CreateRoleRequest request);

    void updateRole(UUID id, UpdateRoleRequest request);

    void assignPermissions(UUID id, AssignPermissionsRequest request);

    void deleteRole(UUID id);
}
