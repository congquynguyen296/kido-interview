package com.kido.corporation.auth.controller;

import com.kido.corporation.auth.dto.common.ApiResponse;
import com.kido.corporation.auth.dto.common.PageResponse;
import com.kido.corporation.auth.dto.request.admin.AdminChangeUserStatusRequest;
import com.kido.corporation.auth.dto.request.admin.AdminCreateUserRequest;
import com.kido.corporation.auth.dto.request.admin.AdminResetPasswordRequest;
import com.kido.corporation.auth.dto.request.admin.AdminUpdateUserRequest;
import com.kido.corporation.auth.dto.request.admin.AdminUserSearchRequest;
import com.kido.corporation.auth.dto.response.admin.AdminChangeUserStatusResponse;
import com.kido.corporation.auth.dto.response.admin.AdminCreateUserResponse;
import com.kido.corporation.auth.dto.response.admin.AdminDeleteUserResponse;
import com.kido.corporation.auth.dto.response.admin.AdminForceLogoutResponse;
import com.kido.corporation.auth.dto.response.admin.AdminResetPasswordResponse;
import com.kido.corporation.auth.dto.response.admin.AdminRestoreUserResponse;
import com.kido.corporation.auth.dto.response.admin.AdminUpdateUserResponse;
import com.kido.corporation.auth.dto.response.admin.AdminUserDetailResponse;
import com.kido.corporation.auth.dto.response.admin.AdminUserListItemResponse;
import com.kido.corporation.auth.dto.response.admin.AdminUserStatisticsResponse;
import com.kido.corporation.auth.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<PageResponse<AdminUserListItemResponse>> searchUsers(@Valid AdminUserSearchRequest request) {
        log.info("Admin: search users - keyword={}, status={}, page={}, size={}",
                request.getKeyword(), request.getStatus(), request.getPage(), request.getSize());
        return ApiResponse.success(adminUserService.searchUsers(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<AdminUserDetailResponse> getUserDetail(@PathVariable UUID id) {
        log.info("Admin: get user detail id={}", id);
        return ApiResponse.success(adminUserService.getUserDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ApiResponse<AdminCreateUserResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        log.info("Admin: create user email={}, roles={}", request.getEmail(), request.getRoles());
        AdminCreateUserResponse response = adminUserService.createUser(request);
        log.info("Admin: user created successfully email={}", request.getEmail());
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ApiResponse<AdminUpdateUserResponse> updateUser(@PathVariable UUID id, @Valid @RequestBody AdminUpdateUserRequest request) {
        log.info("Admin: update user id={}", id);
        adminUserService.updateUser(id, request);
        log.info("Admin: user updated id={}", id);
        return ApiResponse.success(new AdminUpdateUserResponse());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_MANAGE_STATUS')")
    public ApiResponse<AdminChangeUserStatusResponse> changeUserStatus(@PathVariable UUID id, @Valid @RequestBody AdminChangeUserStatusRequest request) {
        log.info("Admin: change status user id={} -> status={}, reason={}", id, request.getStatus(), request.getReason());
        adminUserService.changeUserStatus(id, request);
        log.info("Admin: user status changed id={} -> {}", id, request.getStatus());
        return ApiResponse.success(new AdminChangeUserStatusResponse());
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ApiResponse<AdminResetPasswordResponse> resetPassword(@PathVariable UUID id, @Valid @RequestBody AdminResetPasswordRequest request) {
        log.info("Admin: reset password for user id={}", id);
        adminUserService.resetPassword(id, request);
        log.info("Admin: password reset successfully for user id={}", id);
        return ApiResponse.success(new AdminResetPasswordResponse());
    }

    @PostMapping("/{id}/force-logout")
    @PreAuthorize("hasAuthority('SESSION_REVOKE')")
    public ApiResponse<AdminForceLogoutResponse> forceLogout(@PathVariable UUID id) {
        log.info("Admin: force logout user id={}", id);
        adminUserService.forceLogout(id);
        log.info("Admin: user id={} force-logged out, all sessions revoked", id);
        return ApiResponse.success(new AdminForceLogoutResponse());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ApiResponse<AdminDeleteUserResponse> deleteUser(@PathVariable UUID id) {
        log.info("Admin: soft-delete user id={}", id);
        adminUserService.deleteUser(id);
        log.info("Admin: user id={} soft-deleted", id);
        return ApiResponse.success(new AdminDeleteUserResponse());
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('USER_MANAGE_STATUS')")
    public ApiResponse<AdminRestoreUserResponse> restoreUser(@PathVariable UUID id) {
        log.info("Admin: restore user id={}", id);
        adminUserService.restoreUser(id);
        log.info("Admin: user id={} restored", id);
        return ApiResponse.success(new AdminRestoreUserResponse());
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<AdminUserStatisticsResponse> getStatistics() {
        log.info("Admin: get user statistics");
        return ApiResponse.success(adminUserService.getStatistics());
    }
}
