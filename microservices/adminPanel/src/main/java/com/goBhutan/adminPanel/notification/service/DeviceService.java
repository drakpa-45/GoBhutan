package com.goBhutan.adminPanel.notification.service;

import com.goBhutan.adminPanel.notification.dto.RegisterDeviceRequest;
import com.goBhutan.adminPanel.notification.model.DeviceTokenRecord;
import com.goBhutan.adminPanel.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceTokenRepository repository;

    public DeviceTokenRecord register(String userId, RegisterDeviceRequest request) {
        DeviceTokenRecord record = DeviceTokenRecord.builder()
                .userId(userId)
                .firebaseToken(request.getFirebaseToken())
                .deviceId(request.getDeviceId())
                .platform(request.getPlatform())
                .appName(request.getAppName())
                .appVersion(request.getAppVersion())
                .deviceModel(request.getDeviceModel())
                .permissionGranted(request.getPermissionGranted())
                .active(true)
                .tokenCreatedAt(Instant.now())
                .tokenUpdatedAt(Instant.now())
                .lastSeenAt(Instant.now())
                .build();
        return repository.upsert(userId, record);
    }

    public void logout(String userId, String deviceId) {
        repository.deactivateByDeviceId(deviceId, userId);
    }
}
