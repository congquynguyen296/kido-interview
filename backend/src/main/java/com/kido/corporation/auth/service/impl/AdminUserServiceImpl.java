package com.kido.corporation.auth.service.impl;

import com.kido.corporation.auth.constant.AuthProvider;
import com.kido.corporation.auth.constant.EntityStatus;
import com.kido.corporation.auth.constant.HTTPCode;
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
import com.kido.corporation.auth.entity.Role;
import com.kido.corporation.auth.entity.User;
import com.kido.corporation.auth.exception.AppException;
import com.kido.corporation.auth.mapper.AdminUserMapper;
import com.kido.corporation.auth.repository.RoleRepository;
import com.kido.corporation.auth.repository.UserRepository;
import com.kido.corporation.auth.service.AdminUserService;
import com.kido.corporation.auth.service.CurrentUserService;
import com.kido.corporation.auth.service.MailService;
import com.kido.corporation.auth.service.TokenBlacklistService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final CurrentUserService currentUserService;
    private final MailService mailService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserListItemResponse> searchUsers(AdminUserSearchRequest request) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String pattern = "%" + request.getKeyword().trim().toLowerCase() + "%";
                Predicate emailLike = cb.like(cb.lower(root.get("email")), pattern);
                Predicate usernameLike = cb.like(cb.lower(root.get("username")), pattern);
                Predicate fullNameLike = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate phoneLike = cb.like(cb.lower(root.get("phoneNumber")), pattern);
                predicates.add(cb.or(emailLike, usernameLike, fullNameLike, phoneLike));
            }

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getProvider() != null) {
                predicates.add(cb.equal(root.get("authProvider"), request.getProvider()));
            }

            if (request.getEmailVerified() != null) {
                predicates.add(cb.equal(root.get("emailVerified"), request.getEmailVerified()));
            }

            if (request.getCreatedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedFrom()));
            }

            if (request.getCreatedTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedTo()));
            }

            if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
                Join<User, Role> rolesJoin = root.join("roles");
                predicates.add(cb.equal(rolesJoin.get("name"), request.getRole()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = request.toPageable("createdAt", org.springframework.data.domain.Sort.Direction.DESC);
        Page<User> page = userRepository.findAll(spec, pageable);

        List<AdminUserListItemResponse> content = page.getContent().stream()
                .map(adminUserMapper::toListItemResponse)
                .collect(Collectors.toList());

        return PageResponse.<AdminUserListItemResponse>builder()
                .content(content)
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HTTPCode.USER_NOT_FOUND));
        
        return adminUserMapper.toDetailResponse(user);
    }

    @Override
    @Transactional
    public AdminCreateUserResponse createUser(AdminCreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new AppException(HTTPCode.EMAIL_ALREADY_EXISTS);
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new AppException(HTTPCode.ROLE_NOT_FOUND, "Role not found: " + roleName));
            roles.add(role);
        }

        String rawPassword = request.getPassword();
        if (rawPassword == null || rawPassword.isEmpty()) {
            rawPassword = generateRandomPassword();
        }

        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .fullName(request.getFullName())
                .roles(roles)
                .status(request.getStatus() != null ? request.getStatus() : EntityStatus.ACTIVE)
                .emailVerified(request.getEmailVerified() != null ? request.getEmailVerified() : false)
                .authProvider(AuthProvider.LOCAL)
                .build();

        user = userRepository.save(user);

        // Trigger email sending for new account creation
        mailService.sendWelcomeEmail(user.getEmail(), user.getFullName(), rawPassword);

        return AdminCreateUserResponse.builder().id(user.getId()).build();
    }

    @Override
    @Transactional
    public void updateUser(UUID id, AdminUpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HTTPCode.USER_NOT_FOUND));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }
        if (request.getRoles() != null) {
            // Check self-demotion
            UUID currentUserId = currentUserService.getCurrentUserId();
            if (currentUserId.equals(id)) {
                boolean hasAdminRole = request.getRoles().contains("ADMIN");
                boolean wasAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
                if (wasAdmin && !hasAdminRole) {
                    throw new AppException(HTTPCode.OPERATION_NOT_ALLOWED, "Cannot remove your own ADMIN role");
                }
            }

            Set<Role> roles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new AppException(HTTPCode.ROLE_NOT_FOUND, "Role not found: " + roleName));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changeUserStatus(UUID id, AdminChangeUserStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HTTPCode.USER_NOT_FOUND));

        UUID currentUserId = currentUserService.getCurrentUserId();
        if (currentUserId.equals(id) && (request.getStatus() == EntityStatus.INACTIVE || request.getStatus() == EntityStatus.LOCKED || request.getStatus() == EntityStatus.DELETED)) {
            throw new AppException(HTTPCode.OPERATION_NOT_ALLOWED, "Cannot lock or deactivate your own account");
        }

        user.setStatus(request.getStatus());
        userRepository.save(user);

        if (request.getStatus() == EntityStatus.LOCKED || request.getStatus() == EntityStatus.INACTIVE || request.getStatus() == EntityStatus.DELETED) {
            tokenBlacklistService.invalidateAllUserSessions(id);
        }
    }

    @Override
    @Transactional
    public void resetPassword(UUID id, AdminResetPasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HTTPCode.USER_NOT_FOUND));

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new AppException(HTTPCode.INVALID_REQUEST, "Cannot reset password for non-local accounts");
        }

        String newPassword = request.getNewPassword();
        if (newPassword == null || newPassword.isEmpty()) {
            newPassword = generateRandomPassword();
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        tokenBlacklistService.invalidateAllUserSessions(id);

        mailService.sendAdminResetPassword(user.getEmail(), user.getFullName(), newPassword);
    }

    @Override
    @Transactional
    public void forceLogout(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HTTPCode.USER_NOT_FOUND));
        tokenBlacklistService.invalidateAllUserSessions(id);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HTTPCode.USER_NOT_FOUND));

        UUID currentUserId = currentUserService.getCurrentUserId();
        if (currentUserId.equals(id)) {
            throw new AppException(HTTPCode.OPERATION_NOT_ALLOWED, "Cannot delete your own account");
        }

        user.setStatus(EntityStatus.DELETED);
        userRepository.save(user);

        tokenBlacklistService.invalidateAllUserSessions(id);
    }

    @Override
    @Transactional
    public void restoreUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HTTPCode.USER_NOT_FOUND));

        if (user.getStatus() != EntityStatus.DELETED) {
            throw new AppException(HTTPCode.INVALID_REQUEST, "User is not deleted");
        }

        user.setStatus(EntityStatus.ACTIVE);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserStatisticsResponse getStatistics() {
        long totalUsers = userRepository.count();
        
        Map<String, Long> usersByStatus = new HashMap<>();
        for (EntityStatus status : EntityStatus.values()) {
            long count = userRepository.count((root, query, cb) -> cb.equal(root.get("status"), status));
            if (count > 0) {
                usersByStatus.put(status.name(), count);
            }
        }
        
        Map<String, Long> usersByProvider = new HashMap<>();
        for (AuthProvider provider : AuthProvider.values()) {
            long count = userRepository.count((root, query, cb) -> cb.equal(root.get("authProvider"), provider));
            if (count > 0) {
                usersByProvider.put(provider.name(), count);
            }
        }

        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        long newUsersLast7Days = userRepository.count((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), sevenDaysAgo));

        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        long newUsersLast30Days = userRepository.count((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), thirtyDaysAgo));

        return AdminUserStatisticsResponse.builder()
                .totalUsers(totalUsers)
                .usersByStatus(usersByStatus)
                .usersByProvider(usersByProvider)
                .newUsersLast7Days(newUsersLast7Days)
                .newUsersLast30Days(newUsersLast30Days)
                .build();
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
