package com.goBhutan.adminPanel.notification.controller;

import com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled;
import com.goBhutan.adminPanel.notification.enums.InboxState;
import com.goBhutan.adminPanel.notification.security.CurrentUserResolver;
import com.goBhutan.adminPanel.notification.service.InboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@NotificationModuleEnabled
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class InboxController {
    private final InboxService service;
    private final CurrentUserResolver users;

    @GetMapping
    public Object list(
            Authentication auth,
            @RequestParam(required = false) InboxState state,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "25") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return service.list(users.resolve(auth).getSubject(), state, category, safeLimit);
    }

    @GetMapping("/{notificationId}")
    public Object get(Authentication auth, @PathVariable String notificationId) {
        return service.get(users.resolve(auth).getSubject(), notificationId);
    }

    @GetMapping("/unread-count")
    public Map<String,Long> unread(Authentication auth) {
        return Map.of("count", service.unreadCount(users.resolve(auth).getSubject()));
    }

    @PatchMapping("/{notificationId}/read")
    public void read(Authentication auth, @PathVariable String notificationId) {
        service.read(users.resolve(auth).getSubject(), notificationId);
    }

    @PatchMapping("/read-all")
    public void readAll(Authentication auth) {
        service.readAll(users.resolve(auth).getSubject());
    }

    @PatchMapping("/{notificationId}/archive")
    public void archive(Authentication auth, @PathVariable String notificationId) {
        service.archive(users.resolve(auth).getSubject(), notificationId);
    }
}
