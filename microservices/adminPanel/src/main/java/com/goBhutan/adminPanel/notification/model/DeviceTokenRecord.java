package com.goBhutan.adminPanel.notification.model;

import com.goBhutan.adminPanel.notification.enums.Platform;
import lombok.*;

import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeviceTokenRecord {
    private String id;
    private String userId;
    private String firebaseToken;
    private String deviceId;
    private Platform platform;
    private String appName;
    private String appVersion;
    private String deviceModel;
    private Boolean permissionGranted;
    private Boolean active;
    private Instant tokenCreatedAt;
    private Instant tokenUpdatedAt;
    private Instant lastSeenAt;
}
