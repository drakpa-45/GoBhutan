package com.goBhutan.adminPanel.notification.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {
    public CurrentUser resolve(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authenticated user required");
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return CurrentUser.builder().subject(jwt.getSubject()).build();
        }
        return CurrentUser.builder().subject(authentication.getName()).build();
    }
}
