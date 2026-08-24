package com.goBhutan.adminPanel.notification.model;

import com.goBhutan.adminPanel.notification.enums.DeliveryOutcome;
import lombok.*;

import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeliveryAttemptRecord {
    private String id;
    private String notificationId;
    private String recipientId;
    private String deviceRecordId;
    private Integer attemptNumber;
    private DeliveryOutcome outcome;
    private String firebaseMessageId;
    private String firebaseErrorCode;
    private String errorMessage;
    private Instant attemptedAt;
}
