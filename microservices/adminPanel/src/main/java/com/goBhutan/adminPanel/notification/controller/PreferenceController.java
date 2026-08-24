package com.goBhutan.adminPanel.notification.controller;

import com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled;
import com.goBhutan.adminPanel.notification.dto.PreferenceRequest;
import com.goBhutan.adminPanel.notification.security.CurrentUserResolver;
import com.goBhutan.adminPanel.notification.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@NotificationModuleEnabled
@RequestMapping("/api/v1/notifications/preferences")
@RequiredArgsConstructor
public class PreferenceController {
    private final PreferenceService service;
    private final CurrentUserResolver users;

    @GetMapping
    public Object get(Authentication auth) {
        return service.get(users.resolve(auth).getSubject());
    }

    @PutMapping
    public Object update(Authentication auth, @RequestBody PreferenceRequest request) {
        return service.update(users.resolve(auth).getSubject(), request);
    }
}
