package com.goBhutan.adminPanel.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("notificationAuthorization")
public class NotificationAuthorization {
    private final String requiredAuthority;

    public NotificationAuthorization(
            @Value("${notification.admin-role:notification_manage}") String adminRole) {
        this.requiredAuthority = "ROLE_" + adminRole;
    }

    public boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(requiredAuthority));
    }
}
