package com.uitalo.desafiobackend.requestsystem.infrastructure.config.security;

import org.springframework.security.core.Authentication;

public final class SecurityRoleUtils {

    private SecurityRoleUtils() {
    }

    public static boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
