package com.kido.corporation.auth.service.impl;

import com.kido.corporation.auth.constant.HTTPCode;
import com.kido.corporation.auth.dto.request.admin.AssignPermissionsRequest;
import com.kido.corporation.auth.dto.request.admin.CreateRoleRequest;
import com.kido.corporation.auth.dto.request.admin.UpdateRoleRequest;
import com.kido.corporation.auth.dto.response.admin.CreateRoleResponse;
import com.kido.corporation.auth.dto.response.admin.RoleDetailResponse;
import com.kido.corporation.auth.dto.response.admin.RoleResponse;
import com.kido.corporation.auth.entity.Permission;
import com.kido.corporation.auth.entity.User;
import com.kido.corporation.auth.entity.Role;
import com.kido.corporation.auth.exception.AppException;
import com.kido.corporation.auth.mapper.RoleMapper;
import com.kido.corporation.auth.repository.PermissionRepository;
import com.kido.corporation.auth.repository.RoleRepository;
import com.kido.corporation.auth.repository.UserRepository;
import com.kido.corporation.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDetailResponse getRoleDetail(UUID id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new AppException(HTTPCode.ROLE_NOT_FOUND));
        
        return roleMapper.toRoleDetailResponse(role);
    }

    @Override
    @Transactional
    public CreateRoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new AppException(HTTPCode.ROLE_ALREADY_EXISTS);
        }

        Set<Permission> permissions = new HashSet<>();
        if (request.getPermissionNames() != null) {
            for (String pName : request.getPermissionNames()) {
                Permission p = permissionRepository.findByName(pName).orElseThrow(() -> new AppException(HTTPCode.PERMISSION_NOT_FOUND));
                permissions.add(p);
            }
        }

        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .systemRole(false)
                .permissions(permissions)
                .build();

        role = roleRepository.save(role);
        return CreateRoleResponse.builder().id(role.getId()).build();
    }

    @Override
    @Transactional
    public void updateRole(UUID id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new AppException(HTTPCode.ROLE_NOT_FOUND));

        if (role.isSystemRole() && !role.getName().equals(request.getName())) {
            throw new AppException(HTTPCode.OPERATION_NOT_ALLOWED, "Cannot change the name of a system role");
        }

        if (!role.getName().equals(request.getName()) && roleRepository.existsByName(request.getName())) {
            throw new AppException(HTTPCode.ROLE_ALREADY_EXISTS);
        }

        role.setName(request.getName());
        role.setDescription(request.getDescription());

        if (request.getPermissionNames() != null) {
            Set<Permission> permissions = new HashSet<>();
            for (String pName : request.getPermissionNames()) {
                Permission p = permissionRepository.findByName(pName).orElseThrow(() -> new AppException(HTTPCode.PERMISSION_NOT_FOUND));
                permissions.add(p);
            }
            role.setPermissions(permissions);
        }

        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void assignPermissions(UUID id, AssignPermissionsRequest request) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new AppException(HTTPCode.ROLE_NOT_FOUND));

        Set<Permission> permissions = new HashSet<>();
        if (request.getPermissionNames() != null) {
            for (String pName : request.getPermissionNames()) {
                Permission p = permissionRepository.findByName(pName).orElseThrow(() -> new AppException(HTTPCode.PERMISSION_NOT_FOUND));
                permissions.add(p);
            }
        }
        role.setPermissions(permissions);
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void deleteRole(UUID id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new AppException(HTTPCode.ROLE_NOT_FOUND));

        if (role.isSystemRole()) {
            throw new AppException(HTTPCode.OPERATION_NOT_ALLOWED, "Cannot delete a system role");
        }

        long usersCount = userRepository.count((root, query, cb) -> {
            jakarta.persistence.criteria.Join<User, Role> rolesJoin = root.join("roles");
            return cb.equal(rolesJoin.get("id"), id);
        });

        if (usersCount > 0) {
            throw new AppException(HTTPCode.ROLE_IN_USE, "Cannot delete role assigned to users");
        }

        roleRepository.delete(role);
    }
}
