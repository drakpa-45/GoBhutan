package com.goBhutan.adminPanel.notification.dto;

import com.goBhutan.adminPanel.notification.enums.NotificationStatus;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationResult {
    private String notificationId;
    private String eventId;
    private NotificationStatus status;
    private boolean duplicate;
    private int recipientCount;
    private int successfulDeviceCount;
    private int failedDeviceCount;
    private String message;
}
