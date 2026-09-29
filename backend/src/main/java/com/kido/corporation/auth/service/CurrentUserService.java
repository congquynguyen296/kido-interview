package com.kido.corporation.auth.service;

import com.kido.corporation.auth.constant.HTTPCode;
import com.kido.corporation.auth.exception.AppException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService {

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(HTTPCode.UNAUTHENTICATED);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID) {
            return (UUID) principal;
        }
        if (principal instanceof String) {
            return UUID.fromString((String) principal);
        }
        throw new AppException(HTTPCode.UNAUTHENTICATED);
    }
}
