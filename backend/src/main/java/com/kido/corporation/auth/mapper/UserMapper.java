package com.kido.corporation.auth.mapper;

import com.kido.corporation.auth.dto.response.user.UserProfileResponse;
import com.kido.corporation.auth.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserProfileResponse toProfileResponse(User user) {
        if (user == null) {
            return null;
        }

        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setAuthProvider(user.getAuthProvider());
        response.setEmailVerified(user.isEmailVerified());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getLastLoginAt());

        if (user.getRoles() != null) {
            response.setRoles(user.getRoles().stream()
                    .map(r -> r.getName())
                    .collect(Collectors.toList()));
            
            response.setPermissions(user.getRoles().stream()
                    .flatMap(r -> r.getPermissions().stream())
                    .map(p -> p.getName())
                    .distinct()
                    .collect(Collectors.toList()));
        }

        return response;
    }
}
