package com.goBhutan.adminPanel.notification.service;

import com.goBhutan.adminPanel.notification.dto.NotificationRequest;
import com.goBhutan.adminPanel.notification.enums.NotificationCategory;
import com.goBhutan.adminPanel.notification.enums.NotificationChannel;
import com.goBhutan.adminPanel.notification.exception.NotificationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationRequestValidatorTest {
    private final NotificationRequestValidator validator = new NotificationRequestValidator();

    @Test
    void acceptsValidRecipientNotification() {
        assertDoesNotThrow(() -> validator.validate(validRequest().build()));
    }

    @Test
    void rejectsMissingProgrammaticFields() {
        NotificationRequest request = validRequest().eventId(" ").build();
        assertThrows(NotificationException.class, () -> validator.validate(request));
    }

    @Test
    void rejectsMultipleTargetsAndDuplicateRecipients() {
        NotificationRequest multiple = validRequest().topic("updates").build();
        assertThrows(NotificationException.class, () -> validator.validate(multiple));

        NotificationRequest duplicates = validRequest()
                .recipientId(null).recipientIds(List.of("u1", "u1")).build();
        assertThrows(NotificationException.class, () -> validator.validate(duplicates));
    }

    @Test
    void rejectsInAppTopicAndSensitivePayload() {
        NotificationRequest topic = validRequest().recipientId(null)
                .topic("updates").channel(NotificationChannel.IN_APP).build();
        assertThrows(NotificationException.class, () -> validator.validate(topic));

        NotificationRequest sensitive = validRequest()
                .data(Map.of("access_token", "secret")).build();
        assertThrows(NotificationException.class, () -> validator.validate(sensitive));
    }

    @Test
    void rejectsExpiredNotification() {
        NotificationRequest request = validRequest()
                .expiresAt(Instant.now().minusSeconds(1)).build();
        assertThrows(NotificationException.class, () -> validator.validate(request));
    }

    @Test
    void rejectsOversizedRenderedPayload() {
        assertThrows(NotificationException.class, () -> validator.validateRendered(
                "Title", "x".repeat(1900), Map.of("content", "y".repeat(1900)), null));
    }

    private NotificationRequest.NotificationRequestBuilder validRequest() {
        return NotificationRequest.builder()
                .eventId("TEST:EVENT:1")
                .recipientId("keycloak-subject")
                .title("Title")
                .body("Body")
                .category(NotificationCategory.CUSTOM)
                .channel(NotificationChannel.PUSH_AND_IN_APP)
                .sourceModule("TEST");
    }
}
