package com.goBhutan.adminPanel.notification.model;

import com.goBhutan.adminPanel.notification.enums.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationTemplateRecord {
    private String code;
    private String title;
    private String body;
    private List<String> supportedVariables;
    private NotificationCategory category;
    private NotificationChannel defaultChannel;
    private String locale;
    private Boolean active;
    private Integer version;
    private Instant updatedAt;
}
