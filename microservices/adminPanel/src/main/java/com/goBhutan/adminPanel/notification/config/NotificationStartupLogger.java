package com.goBhutan.adminPanel.notification.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationStartupLogger {

    private final Environment environment;
    private final ApplicationContext applicationContext;

    @EventListener(ApplicationReadyEvent.class)
    public void logStatus() {
        boolean enabled = environment.getProperty(
                "notification.firebase.enabled", Boolean.class, false);
        String projectId = environment.getProperty(
                "notification.firebase.project-id", "");
        String credentialsLocation = environment.getProperty(
                "notification.firebase.credentials-location", "");
        boolean preferenceControllerRegistered =
                applicationContext.containsBean("preferenceController");

        log.info(
                "Notification module startup status enabled={} projectId={} "
                        + "credentialsLocation={} preferenceControllerRegistered={}",
                enabled, projectId, credentialsLocation, preferenceControllerRegistered);

        if (enabled && !preferenceControllerRegistered) {
            log.error(
                    "Notification module is enabled but PreferenceController was not registered");
        } else if (!enabled) {
            log.warn(
                    "Notification module is disabled. Activate the local profile or set "
                            + "NOTIFICATION_FIREBASE_ENABLED=true");
        }
    }
}
