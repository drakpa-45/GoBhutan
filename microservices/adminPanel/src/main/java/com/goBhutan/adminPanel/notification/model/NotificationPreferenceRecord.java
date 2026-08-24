package com.goBhutan.adminPanel.notification.model;

import com.goBhutan.adminPanel.notification.enums.NotificationCategory;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationPreferenceRecord {
    private String userId;
    private List<NotificationCategory> disabledCategories;
    private Boolean pushEnabled;
    private Instant updatedAt;
}
