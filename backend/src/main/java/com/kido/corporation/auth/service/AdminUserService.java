package com.kido.corporation.auth.service;

import com.kido.corporation.auth.dto.common.PageResponse;
import com.kido.corporation.auth.dto.request.admin.AdminChangeUserStatusRequest;
import com.kido.corporation.auth.dto.request.admin.AdminCreateUserRequest;
import com.kido.corporation.auth.dto.request.admin.AdminResetPasswordRequest;
import com.kido.corporation.auth.dto.request.admin.AdminUpdateUserRequest;
import com.kido.corporation.auth.dto.request.admin.AdminUserSearchRequest;
import com.kido.corporation.auth.dto.response.admin.AdminCreateUserResponse;
import com.kido.corporation.auth.dto.response.admin.AdminUserDetailResponse;
import com.kido.corporation.auth.dto.response.admin.AdminUserListItemResponse;
import com.kido.corporation.auth.dto.response.admin.AdminUserStatisticsResponse;

import java.util.UUID;

public interface AdminUserService {

    PageResponse<AdminUserListItemResponse> searchUsers(AdminUserSearchRequest request);

    AdminUserDetailResponse getUserDetail(UUID id);

    AdminCreateUserResponse createUser(AdminCreateUserRequest request);

    void updateUser(UUID id, AdminUpdateUserRequest request);

    void changeUserStatus(UUID id, AdminChangeUserStatusRequest request);

    void resetPassword(UUID id, AdminResetPasswordRequest request);

    void forceLogout(UUID id);

    void deleteUser(UUID id);

    void restoreUser(UUID id);

    AdminUserStatisticsResponse getStatistics();
}
