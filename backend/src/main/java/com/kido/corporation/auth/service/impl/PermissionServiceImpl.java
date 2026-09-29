package com.kido.corporation.auth.service.impl;

import com.kido.corporation.auth.constant.HTTPCode;
import com.kido.corporation.auth.dto.request.admin.CreatePermissionRequest;
import com.kido.corporation.auth.dto.request.admin.UpdatePermissionRequest;
import com.kido.corporation.auth.dto.response.admin.CreatePermissionResponse;
import com.kido.corporation.auth.dto.response.admin.PermissionResponse;
import com.kido.corporation.auth.entity.Permission;
import com.kido.corporation.auth.exception.AppException;
import com.kido.corporation.auth.mapper.PermissionMapper;
import com.kido.corporation.auth.repository.PermissionRepository;
import com.kido.corporation.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toPermissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CreatePermissionResponse createPermission(CreatePermissionRequest request) {
        if (permissionRepository.existsByName(request.getName())) {
            throw new AppException(HTTPCode.PERMISSION_ALREADY_EXISTS);
        }

        Permission permission = Permission.builder()
                .name(request.getName())
                .description(request.getDescription())
                .resource(request.getResource())
                .action(request.getAction())
                .build();

        permission = permissionRepository.save(permission);
        return CreatePermissionResponse.builder().id(permission.getId()).build();
    }

    @Override
    @Transactional
    public void updatePermission(UUID id, UpdatePermissionRequest request) {
        Permission permission = permissionRepository.findById(id).orElseThrow(() -> new AppException(HTTPCode.PERMISSION_NOT_FOUND));

        if (request.getDescription() != null) {
            permission.setDescription(request.getDescription());
        }
        if (request.getResource() != null) {
            permission.setResource(request.getResource());
        }
        if (request.getAction() != null) {
            permission.setAction(request.getAction());
        }

        permissionRepository.save(permission);
    }

    @Override
    @Transactional
    public void deletePermission(UUID id) {
        Permission permission = permissionRepository.findById(id).orElseThrow(() -> new AppException(HTTPCode.PERMISSION_NOT_FOUND));

        // It is a good practice to not delete permissions if they are associated with any role,
        // but typically a permission deletion just removes it from the join table. Let's rely on cascade or throw error.
        // If we want to strictly prevent, we could query roles containing this permission.
        // In this basic version, we will just delete it, or wait, many-to-many relationship might fail if not unlinked.
        // Let's just do delete and if it fails constraint it throws error.
        try {
            permissionRepository.delete(permission);
        } catch (Exception e) {
            throw new AppException(HTTPCode.OPERATION_NOT_ALLOWED, "Cannot delete permission. It might be in use.");
        }
    }
}
