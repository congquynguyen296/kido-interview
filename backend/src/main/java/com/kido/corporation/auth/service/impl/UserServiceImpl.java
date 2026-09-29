package com.kido.corporation.auth.service.impl;

import com.kido.corporation.auth.constant.AuthProvider;
import com.kido.corporation.auth.constant.EntityStatus;
import com.kido.corporation.auth.constant.HTTPCode;
import com.kido.corporation.auth.dto.request.user.ChangePasswordRequest;
import com.kido.corporation.auth.dto.request.user.DeactivateAccountRequest;
import com.kido.corporation.auth.dto.request.user.UpdateProfileRequest;
import com.kido.corporation.auth.dto.response.user.UserProfileResponse;
import com.kido.corporation.auth.entity.User;
import com.kido.corporation.auth.exception.AppException;
import com.kido.corporation.auth.mapper.UserMapper;
import com.kido.corporation.auth.repository.UserRepository;
import com.kido.corporation.auth.service.CurrentUserService;
import com.kido.corporation.auth.service.TokenBlacklistService;
import com.kido.corporation.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMe() {
        UUID userId = currentUserService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HTTPCode.USER_NOT_FOUND));
        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        UUID userId = currentUserService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HTTPCode.USER_NOT_FOUND));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        user = userRepository.save(user);
        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        UUID userId = currentUserService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HTTPCode.USER_NOT_FOUND));

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new AppException(HTTPCode.INVALID_REQUEST, "Cannot change password for non-local accounts");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(HTTPCode.OLD_PASSWORD_INCORRECT);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(HTTPCode.PASSWORD_MISMATCH);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new AppException(HTTPCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        tokenBlacklistService.invalidateAllUserSessions(userId);
    }

    @Override
    @Transactional
    public void deactivateAccount(DeactivateAccountRequest request) {
        UUID userId = currentUserService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HTTPCode.USER_NOT_FOUND));

        if (user.getAuthProvider() == AuthProvider.LOCAL) {
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                throw new AppException(HTTPCode.INVALID_REQUEST, "Password is required to deactivate local account");
            }
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                throw new AppException(HTTPCode.INVALID_CREDENTIALS, "Incorrect password");
            }
        }

        // Prevent admin from deactivating themselves? 
        // According to docs: "admin không được tự khoá/xoá chính mình" -> we can check roles
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
        if (isAdmin) {
            throw new AppException(HTTPCode.OPERATION_NOT_ALLOWED, "Administrators cannot deactivate their own accounts");
        }

        user.setStatus(EntityStatus.INACTIVE);
        userRepository.save(user);

        tokenBlacklistService.invalidateAllUserSessions(userId);
    }
}
