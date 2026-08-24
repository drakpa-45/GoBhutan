package com.goBhutan.adminPanel.notification.event;

import com.goBhutan.adminPanel.notification.dto.NotificationRequest;
import com.goBhutan.adminPanel.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
@RequiredArgsConstructor
@Slf4j
public class NotificationAfterCommitPublisher {
    private final NotificationService notificationService;

    public void sendAfterCommit(NotificationRequest request) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            notificationService.send(request);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    notificationService.send(request);
                } catch (RuntimeException e) {
                    log.error("Unable to enqueue notification after business transaction committed eventId={}",
                            request == null ? null : request.getEventId(), e);
                }
            }
        });
    }
}
