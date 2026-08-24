package com.goBhutan.adminPanel.notification.dto;

import com.goBhutan.adminPanel.notification.enums.NotificationCategory;
import lombok.Data;
import java.util.Set;

@Data
public class PreferenceRequest {
    private Boolean pushEnabled;
    private Set<NotificationCategory> disabledCategories;
}
