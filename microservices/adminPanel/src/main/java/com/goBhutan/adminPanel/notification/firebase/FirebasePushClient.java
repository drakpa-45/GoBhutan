package com.goBhutan.adminPanel.notification.firebase;

import com.goBhutan.adminPanel.notification.enums.*;
import com.goBhutan.adminPanel.notification.model.NotificationEventRecord;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
@RequiredArgsConstructor
public class FirebasePushClient {
    private final FirebaseMessaging messaging;

    public FirebaseDeliveryResult sendToToken(
            String token, NotificationEventRecord event, String inboxId) {
        try {
            Message.Builder builder = Message.builder()
                    // Firebase Admin 9.10 deprecates token targeting in favor of FID,
                    // but registration-token delivery remains supported for existing clients.
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(event.getTitle())
                            .setBody(event.getBody())
                            .setImage(event.getImageUrl())
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(event.getPriority() == NotificationPriority.HIGH
                                    ? AndroidConfig.Priority.HIGH : AndroidConfig.Priority.NORMAL)
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder().setSound("default").build())
                            .build());

            if (event.getData() != null) builder.putAllData(event.getData());
            if (event.getNotificationId() != null) builder.putData("notificationId", event.getNotificationId());
            if (inboxId != null) builder.putData("inboxId", inboxId);
            if (event.getCategory() != null) builder.putData("category", event.getCategory().name());
            if (event.getActionType() != null) builder.putData("actionType", event.getActionType());
            if (event.getActionValue() != null) builder.putData("actionValue", event.getActionValue());

            String messageId = messaging.send(builder.build());
            return FirebaseDeliveryResult.builder()
                    .outcome(DeliveryOutcome.ACCEPTED)
                    .messageId(messageId).build();
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode code = e.getMessagingErrorCode();
            DeliveryOutcome outcome;
            if (code == MessagingErrorCode.UNREGISTERED) {
                outcome = DeliveryOutcome.INVALID_TOKEN;
            } else if (code == MessagingErrorCode.UNAVAILABLE
                    || code == MessagingErrorCode.INTERNAL
                    || code == MessagingErrorCode.QUOTA_EXCEEDED) {
                outcome = DeliveryOutcome.TRANSIENT_FAILURE;
            } else {
                outcome = DeliveryOutcome.PERMANENT_FAILURE;
            }
            return FirebaseDeliveryResult.builder()
                    .outcome(outcome)
                    .errorCode(code == null ? "UNKNOWN" : code.name())
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    public FirebaseDeliveryResult sendToTopic(String topic, NotificationEventRecord event) {
        try {
            Message.Builder builder = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(event.getTitle())
                            .setBody(event.getBody())
                            .setImage(event.getImageUrl()).build());
            if (event.getData() != null) builder.putAllData(event.getData());
            if (event.getNotificationId() != null) builder.putData("notificationId", event.getNotificationId());
            if (event.getCategory() != null) builder.putData("category", event.getCategory().name());
            if (event.getActionType() != null) builder.putData("actionType", event.getActionType());
            if (event.getActionValue() != null) builder.putData("actionValue", event.getActionValue());
            String id = messaging.send(builder.build());
            return FirebaseDeliveryResult.builder()
                    .outcome(DeliveryOutcome.ACCEPTED).messageId(id).build();
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode code = e.getMessagingErrorCode();
            DeliveryOutcome outcome = code == MessagingErrorCode.UNAVAILABLE
                    || code == MessagingErrorCode.INTERNAL
                    || code == MessagingErrorCode.QUOTA_EXCEEDED
                    ? DeliveryOutcome.TRANSIENT_FAILURE
                    : DeliveryOutcome.PERMANENT_FAILURE;
            return FirebaseDeliveryResult.builder()
                    .outcome(outcome)
                    .errorCode(code == null ? "UNKNOWN" : code.name())
                    .errorMessage(e.getMessage()).build();
        }
    }
}
