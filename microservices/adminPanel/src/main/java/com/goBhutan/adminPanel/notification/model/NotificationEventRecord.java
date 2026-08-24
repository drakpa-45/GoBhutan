package com.goBhutan.adminPanel.notification.model;

import com.goBhutan.adminPanel.notification.enums.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationEventRecord {
    private String notificationId;
    private String eventId;
    private List<String> recipientIds;
    private String topic;
    private String title;
    private String body;
    private NotificationCategory category;
    private NotificationChannel channel;
    private String sourceModule;
    private String sourceEntityType;
    private String sourceEntityId;
    private String actionType;
    private String actionValue;
    private String imageUrl;
    private NotificationPriority priority;
    private Map<String,String> data;
    private Instant scheduledAt;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant sentAt;
    private NotificationStatus status;
    private String failureReason;
    private Integer retryCount;
    private Instant nextAttemptAt;
    private Instant leaseUntil;
    private String leaseId;
    private String templateCode;
}
