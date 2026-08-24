package com.goBhutan.adminPanel.notification.service;

import com.goBhutan.adminPanel.notification.config.NotificationProperties;
import com.goBhutan.adminPanel.notification.enums.*;
import com.goBhutan.adminPanel.notification.firebase.*;
import com.goBhutan.adminPanel.notification.model.*;
import com.goBhutan.adminPanel.notification.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
@RequiredArgsConstructor
public class NotificationProcessor {
    private static final Set<NotificationCategory> MANDATORY_CATEGORIES = Set.of(
            NotificationCategory.PAYMENT_SUCCESS,
            NotificationCategory.PAYMENT_FAILED,
            NotificationCategory.SYSTEM_ALERT);
    private final NotificationProperties properties;
    private final NotificationEventRepository eventRepository;
    private final UserNotificationRepository inboxRepository;
    private final DeviceTokenRepository deviceRepository;
    private final DeliveryAttemptRepository attemptRepository;
    private final PreferenceRepository preferenceRepository;
    private final FirebasePushClient pushClient;

    @Async("notificationTaskExecutor")
    public void processAsync(NotificationEventRecord event) {
        process(event);
    }

    public void process(NotificationEventRecord event) {
        Optional<NotificationEventRecord> claimed = eventRepository.acquireLease(
                event, properties.getScheduler().getLeaseSeconds());
        if (claimed.isEmpty()) return;
        event = claimed.get();

        Instant started = Instant.now();
        try {
            if (event.getExpiresAt() != null && event.getExpiresAt().isBefore(Instant.now())) {
                event.setStatus(NotificationStatus.EXPIRED);
                event.setFailureReason("Notification expired before delivery");
                eventRepository.completeIfLeaseOwned(event);
                return;
            }

            int success = 0;
            int failure = 0;
            int skipped = 0;
            int inboxSuccess = 0;
            boolean transientFailure = false;
            Set<String> acceptedTargets = attemptRepository.findAcceptedTargets(
                    event.getNotificationId());

            if (event.getTopic() != null) {
                String targetId = "topic:" + event.getTopic();
                if (acceptedTargets.contains(targetId)) {
                    success++;
                } else {
                    FirebaseDeliveryResult r = pushClient.sendToTopic(event.getTopic(), event);
                    saveAttempt(event, null, targetId, r);
                    if (r.getOutcome() == DeliveryOutcome.ACCEPTED) success++;
                    else {
                        failure++;
                        transientFailure = r.getOutcome() == DeliveryOutcome.TRANSIENT_FAILURE;
                    }
                }
            } else {
                for (String recipientId : Optional.ofNullable(event.getRecipientIds()).orElse(List.of())) {
                    if (event.getChannel() == NotificationChannel.IN_APP
                            || event.getChannel() == NotificationChannel.PUSH_AND_IN_APP) {
                        saveInbox(event, recipientId);
                        inboxSuccess++;
                    }

                    if (event.getChannel() == NotificationChannel.PUSH
                            || event.getChannel() == NotificationChannel.PUSH_AND_IN_APP) {
                        NotificationPreferenceRecord pref =
                                preferenceRepository.find(recipientId).orElse(null);
                        boolean mandatory = MANDATORY_CATEGORIES.contains(event.getCategory());
                        boolean pushAllowed = mandatory || pref == null
                                || !Boolean.FALSE.equals(pref.getPushEnabled());
                        boolean categoryAllowed = mandatory || pref == null
                                || pref.getDisabledCategories() == null
                                || !pref.getDisabledCategories().contains(event.getCategory());

                        if (pushAllowed && categoryAllowed) {
                            List<DeviceTokenRecord> devices = deviceRepository.findActiveByUserId(recipientId);
                            if (devices.isEmpty()) skipped++;
                            for (DeviceTokenRecord device : devices) {
                                if (acceptedTargets.contains(device.getId())) {
                                    success++;
                                    continue;
                                }
                                String inboxId = event.getChannel() == NotificationChannel.PUSH_AND_IN_APP
                                        ? inboxId(event, recipientId) : null;
                                FirebaseDeliveryResult r = pushClient.sendToToken(
                                        device.getFirebaseToken(), event, inboxId);
                                saveAttempt(event, recipientId, device.getId(), r);
                                if (r.getOutcome() == DeliveryOutcome.ACCEPTED) {
                                    success++;
                                } else {
                                    failure++;
                                    if (r.getOutcome() == DeliveryOutcome.INVALID_TOKEN) {
                                        deviceRepository.deactivate(device.getId());
                                    }
                                    if (r.getOutcome() == DeliveryOutcome.TRANSIENT_FAILURE) {
                                        transientFailure = true;
                                    }
                                }
                            }
                        } else {
                            skipped++;
                        }
                    }
                }
            }

            int currentRetry = Optional.ofNullable(event.getRetryCount()).orElse(0);
            if (transientFailure && currentRetry < properties.getRetry().getMaxAttempts()) {
                int retry = currentRetry + 1;
                long delay = properties.getRetry().getInitialDelaySeconds() * (1L << (retry - 1));
                event.setRetryCount(retry);
                event.setNextAttemptAt(Instant.now().plusSeconds(delay));
                event.setStatus(NotificationStatus.PENDING);
                event.setFailureReason("Transient FCM failure; retry scheduled");
            } else if (failure == 0 && skipped == 0) {
                event.setStatus(NotificationStatus.SENT);
                event.setSentAt(Instant.now());
                event.setFailureReason(null);
            } else if (success > 0 || inboxSuccess > 0) {
                event.setStatus(NotificationStatus.PARTIALLY_SENT);
                event.setSentAt(Instant.now());
                event.setFailureReason(skipped > 0
                        ? "Some push deliveries had no eligible device"
                        : "Some device deliveries failed");
            } else {
                event.setStatus(NotificationStatus.FAILED);
                event.setFailureReason(skipped > 0 && failure == 0
                        ? "No eligible device accepted the notification"
                        : "All push deliveries failed");
            }
            boolean completed = eventRepository.completeIfLeaseOwned(event);

            log.info("notification processed notificationId={} eventId={} source={} success={} failure={} skipped={} completed={} durationMs={}",
                    event.getNotificationId(), event.getEventId(), event.getSourceModule(),
                    success, failure, skipped, completed,
                    java.time.Duration.between(started, Instant.now()).toMillis());
        } catch (Exception e) {
            event.setStatus(NotificationStatus.FAILED);
            event.setFailureReason(safe(e.getMessage()));
            eventRepository.completeIfLeaseOwned(event);
            log.error("notification processing failed notificationId={} eventId={}",
                    event.getNotificationId(), event.getEventId(), e);
        }
    }

    private void saveInbox(NotificationEventRecord event, String recipientId) {
        String id = inboxId(event, recipientId);
        UserNotificationRecord record = UserNotificationRecord.builder()
                .id(id)
                .notificationId(event.getNotificationId())
                .recipientId(recipientId)
                .title(event.getTitle())
                .body(event.getBody())
                .category(event.getCategory())
                .sourceModule(event.getSourceModule())
                .sourceEntityType(event.getSourceEntityType())
                .sourceEntityId(event.getSourceEntityId())
                .actionType(event.getActionType())
                .actionValue(event.getActionValue())
                .imageUrl(event.getImageUrl())
                .data(event.getData())
                .state(InboxState.UNREAD)
                .createdAt(event.getCreatedAt())
                .build();
        inboxRepository.saveIfAbsent(record);
    }

    private String inboxId(NotificationEventRecord event, String recipientId) {
        return event.getNotificationId() + "_" + recipientId;
    }

    private void saveAttempt(
            NotificationEventRecord event, String recipientId,
            String targetId, FirebaseDeliveryResult result) {
        DeliveryAttemptRecord attempt = DeliveryAttemptRecord.builder()
                .id(UUID.randomUUID().toString())
                .notificationId(event.getNotificationId())
                .recipientId(recipientId)
                .deviceRecordId(targetId)
                .attemptNumber(Optional.ofNullable(event.getRetryCount()).orElse(0) + 1)
                .outcome(result.getOutcome())
                .firebaseMessageId(result.getMessageId())
                .firebaseErrorCode(result.getErrorCode())
                .errorMessage(safe(result.getErrorMessage()))
                .attemptedAt(Instant.now())
                .build();
        attemptRepository.save(attempt);
    }

    private String safe(String value) {
        if (value == null) return null;
        return value.length() > 500 ? value.substring(0, 500) : value;
    }
}
