package com.kido.corporation.auth.config;

import com.kido.corporation.auth.constant.EntityStatus;
import com.kido.corporation.auth.constant.PredefinedPermission;
import com.kido.corporation.auth.constant.PredefinedRole;
import com.kido.corporation.auth.entity.Permission;
import com.kido.corporation.auth.entity.Role;
import com.kido.corporation.auth.entity.User;
import com.kido.corporation.auth.repository.PermissionRepository;
import com.kido.corporation.auth.repository.RoleRepository;
import com.kido.corporation.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AdminBootstrapProperties adminBootstrapProperties;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initPermissions();
        initRoles();
        initAdminUser();
    }

    private void initPermissions() {
        for (String permName : PredefinedPermission.ROLE_PERMISSIONS.get(PredefinedRole.ADMIN)) {
            if (permissionRepository.findByName(permName).isEmpty()) {
                Permission permission = Permission.builder()
                        .name(permName)
                        .description("Permission for " + permName)
                        .build();
                permissionRepository.save(permission);
                log.info("Created permission: {}", permName);
            }
        }
    }

    private void initRoles() {
        for (Map.Entry<String, List<String>> entry : PredefinedPermission.ROLE_PERMISSIONS.entrySet()) {
            String roleName = entry.getKey();
            List<String> permNames = entry.getValue();

            Role role = roleRepository.findByName(roleName).orElseGet(() -> {
                Role newRole = Role.builder()
                        .name(roleName)
                        .description("System role: " + roleName)
                        .systemRole(true)
                        .build();
                log.info("Created role: {}", roleName);
                return roleRepository.save(newRole);
            });

            // Update permissions
            Set<Permission> permissions = new HashSet<>();
            for (String permName : permNames) {
                permissionRepository.findByName(permName).ifPresent(permissions::add);
            }
            role.setPermissions(permissions);
            roleRepository.save(role);
        }
    }

    private void initAdminUser() {
        if (!adminBootstrapProperties.isEnabled()) {
            return;
        }

        String adminEmail = adminBootstrapProperties.getEmail();
        if (adminEmail == null || adminEmail.isBlank()) {
            log.warn("Admin bootstrap is enabled but email is not configured");
            return;
        }

        if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
            log.info("Admin user {} already exists", adminEmail);
            return;
        }

        Role adminRole = roleRepository.findByName(PredefinedRole.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

        User admin = User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminBootstrapProperties.getPassword()))
                .fullName(adminBootstrapProperties.getFullName())
                .status(EntityStatus.ACTIVE)
                .emailVerified(true)
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(admin);
        log.info("Bootstrap admin user created successfully: {}", adminEmail);
    }
}
