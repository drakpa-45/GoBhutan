package com.goBhutan.adminPanel.notification.dto;

import com.goBhutan.adminPanel.notification.enums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationRequest {
    @NotBlank private String eventId;
    private String recipientId;
    private List<String> recipientIds;
    private String topic;
    private String title;
    private String body;
    @NotNull private NotificationCategory category;
    @NotNull private NotificationChannel channel;
    @NotBlank private String sourceModule;
    private String sourceEntityType;
    private String sourceEntityId;
    private String actionType;
    private String actionValue;
    private String imageUrl;
    @Builder.Default private NotificationPriority priority = NotificationPriority.NORMAL;
    private Map<String,String> data;
    private Instant scheduledAt;
    private Instant expiresAt;
    private String templateCode;
    private Map<String,String> templateVariables;
}
