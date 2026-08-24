package com.goBhutan.adminPanel.notification.scheduler;

import com.goBhutan.adminPanel.notification.config.NotificationProperties;
import com.goBhutan.adminPanel.notification.model.NotificationEventRecord;
import com.goBhutan.adminPanel.notification.repository.NotificationEventRepository;
import com.goBhutan.adminPanel.notification.service.NotificationProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
@RequiredArgsConstructor
public class NotificationScheduler {
    private final NotificationProperties properties;
    private final NotificationEventRepository repository;
    private final NotificationProcessor processor;

    @Scheduled(fixedDelayString = "${notification.scheduler.fixed-delay-ms:15000}")
    public void processDue() {
        for (NotificationEventRecord event : repository.findDue(
                Math.max(1, Math.min(properties.getScheduler().getBatchSize(), 500)))) {
            processor.processAsync(event);
        }
    }
}
