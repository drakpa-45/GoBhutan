package com.goBhutan.adminPanel.notification.scheduler;

import com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled;
import com.goBhutan.adminPanel.notification.config.NotificationProperties;
import com.goBhutan.adminPanel.notification.repository.DeliveryAttemptRepository;
import com.goBhutan.adminPanel.notification.repository.NotificationEventRepository;
import com.goBhutan.adminPanel.notification.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@NotificationModuleEnabled
@RequiredArgsConstructor
public class NotificationRetentionScheduler {
    private static final int DELETE_BATCH_SIZE = 400;

    private final NotificationProperties properties;
    private final NotificationEventRepository eventRepository;
    private final UserNotificationRepository inboxRepository;
    private final DeliveryAttemptRepository attemptRepository;

    @Scheduled(cron = "${notification.retention.cron:0 30 2 * * *}",
            zone = "${notification.retention.zone:Asia/Thimphu}")
    public void cleanExpiredRecords() {
        Instant now = Instant.now();
        int events = eventRepository.deleteTerminalBefore(now.minus(
                properties.getRetention().getNotificationDays(), ChronoUnit.DAYS), DELETE_BATCH_SIZE);
        int inbox = inboxRepository.deleteBefore(now.minus(
                properties.getRetention().getNotificationDays(), ChronoUnit.DAYS), DELETE_BATCH_SIZE);
        int attempts = attemptRepository.deleteBefore(now.minus(
                properties.getRetention().getDeliveryAttemptDays(), ChronoUnit.DAYS), DELETE_BATCH_SIZE);
        log.info("notification retention cleanup events={} inbox={} attempts={}",
                events, inbox, attempts);
    }
}
