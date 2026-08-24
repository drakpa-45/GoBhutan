package com.goBhutan.adminPanel.notification.controller;

import com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled;
import com.goBhutan.adminPanel.notification.dto.*;
import com.goBhutan.adminPanel.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@NotificationModuleEnabled
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
//@PreAuthorize("hasAuthority('ROLE_${notification.admin-role:notification_manage}') "
 //       + "or hasAuthority('${notification.admin-role:notification_manage}')")

@PreAuthorize("@notificationAuthorization.isAdmin(authentication)")
public class AdminNotificationController {
    private final NotificationService service;

    @PostMapping
    public NotificationResult send(@Valid @RequestBody NotificationRequest request) {
        return request.getScheduledAt() == null
                ? service.send(request)
                : service.schedule(request);
    }

    @PostMapping("/{notificationId}/retry")
    public void retry(@PathVariable String notificationId) {
        service.retry(notificationId);
    }

    @PostMapping("/{notificationId}/cancel")
    public void cancel(@PathVariable String notificationId) {
        service.cancel(notificationId);
    }
}
