package com.goBhutan.adminPanel.notification.firebase;

import com.goBhutan.adminPanel.notification.enums.DeliveryOutcome;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FirebaseDeliveryResult {
    private DeliveryOutcome outcome;
    private String messageId;
    private String errorCode;
    private String errorMessage;
}
