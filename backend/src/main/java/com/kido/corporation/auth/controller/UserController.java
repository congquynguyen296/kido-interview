package com.kido.corporation.auth.controller;

import com.kido.corporation.auth.dto.common.ApiResponse;
import com.kido.corporation.auth.dto.response.user.UserProfileResponse;
import com.kido.corporation.auth.dto.request.user.ChangePasswordRequest;
import com.kido.corporation.auth.dto.request.user.DeactivateAccountRequest;
import com.kido.corporation.auth.dto.request.user.UpdateProfileRequest;
import com.kido.corporation.auth.dto.response.user.ChangePasswordResponse;
import com.kido.corporation.auth.dto.response.user.DeactivateAccountResponse;

import com.kido.corporation.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getCurrentUser() {
        log.debug("Get current user profile request");
        UserProfileResponse profile = userService.getMe();
        log.debug("Profile fetched for user: {}", profile.getEmail());
        return ApiResponse.success(profile);
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        log.info("Update profile request");
        UserProfileResponse response = userService.updateProfile(request);
        log.info("Profile updated successfully");
        return ApiResponse.success(response);
    }

    @PatchMapping("/me/password")
    public ApiResponse<ChangePasswordResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password request");
        userService.changePassword(request);
        log.info("Password changed successfully, all old sessions invalidated");
        return ApiResponse.success(new ChangePasswordResponse());
    }

    @DeleteMapping("/me")
    public ApiResponse<DeactivateAccountResponse> deactivateAccount(@Valid @RequestBody DeactivateAccountRequest request) {
        log.info("Deactivate account request");
        userService.deactivateAccount(request);
        log.info("Account deactivated successfully");
        return ApiResponse.success(new DeactivateAccountResponse());
    }
}
