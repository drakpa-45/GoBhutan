package com.goBhutan.adminPanel.notification.service;

import com.goBhutan.adminPanel.notification.dto.NotificationRequest;
import com.goBhutan.adminPanel.notification.dto.NotificationResult;

public interface NotificationService {
    NotificationResult send(NotificationRequest request);
    NotificationResult schedule(NotificationRequest request);
    void cancel(String notificationId);
    void retry(String notificationId);
}
