package com.goBhutan.adminPanel.notification.controller;

import com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled;
import com.goBhutan.adminPanel.notification.dto.RegisterDeviceRequest;
import com.goBhutan.adminPanel.notification.model.DeviceTokenRecord;
import com.goBhutan.adminPanel.notification.security.CurrentUserResolver;
import com.goBhutan.adminPanel.notification.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@NotificationModuleEnabled
@RequestMapping("/api/v1/notifications/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService service;
    private final CurrentUserResolver users;

    @PostMapping
    public ResponseEntity<?> register(
            Authentication authentication,
            @Valid @RequestBody RegisterDeviceRequest request) {
        String userId = users.resolve(authentication).getSubject();
        DeviceTokenRecord saved = service.register(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "deviceId", saved.getDeviceId(),
                "platform", saved.getPlatform(),
                "active", saved.getActive()
        ));
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> deactivate(
            Authentication authentication,
            @PathVariable String deviceId) {
        service.logout(users.resolve(authentication).getSubject(), deviceId);
        return ResponseEntity.noContent().build();
    }
}
