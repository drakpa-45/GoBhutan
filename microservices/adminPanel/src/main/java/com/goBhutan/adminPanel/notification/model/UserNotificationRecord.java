package com.goBhutan.adminPanel.notification.model;

import com.goBhutan.adminPanel.notification.enums.*;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserNotificationRecord {
    private String id;
    private String notificationId;
    private String recipientId;
    private String title;
    private String body;
    private NotificationCategory category;
    private String sourceModule;
    private String sourceEntityType;
    private String sourceEntityId;
    private String actionType;
    private String actionValue;
    private String imageUrl;
    private Map<String,String> data;
    private InboxState state;
    private Instant createdAt;
    private Instant readAt;
    private Instant archivedAt;
}
