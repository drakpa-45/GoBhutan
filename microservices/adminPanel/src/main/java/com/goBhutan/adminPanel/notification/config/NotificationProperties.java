package com.goBhutan.adminPanel.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {
    private Firebase firebase = new Firebase();
    private Retry retry = new Retry();
    private Retention retention = new Retention();
    private Scheduler scheduler = new Scheduler();
    private Async async = new Async();
    private String adminRole = "notification_manage";

    @Data public static class Firebase {
        private boolean enabled = false;
        private String projectId;
        private String credentialsLocation;
    }
    @Data public static class Retry {
        private int maxAttempts = 3;
        private long initialDelaySeconds = 30;
    }
    @Data public static class Retention {
        private int notificationDays = 90;
        private int deliveryAttemptDays = 30;
    }
    @Data public static class Scheduler {
        private long fixedDelayMs = 15000;
        private long leaseSeconds = 120;
        private int batchSize = 100;
    }
    @Data public static class Async {
        private int corePoolSize = 4;
        private int maxPoolSize = 8;
        private int queueCapacity = 500;
    }
}
