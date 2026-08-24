package com.goBhutan.adminPanel.notification.service;

import com.goBhutan.adminPanel.notification.dto.NotificationRequest;
import com.goBhutan.adminPanel.notification.exception.NotificationException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
public class NotificationRequestValidator {
    private static final Pattern TOPIC = Pattern.compile("[a-zA-Z0-9-_.~%]{1,900}");
    private static final Set<String> RESERVED_DATA_KEYS = Set.of(
            "from", "message_type", "collapse_key");

    public void validate(NotificationRequest r) {
        if (r == null) throw new NotificationException("Notification request is required");
        requireText(r.getEventId(), "eventId");
        requireText(r.getSourceModule(), "sourceModule");
        if (r.getCategory() == null) throw new NotificationException("category is required");
        if (r.getChannel() == null) throw new NotificationException("channel is required");
        if (r.getPriority() == null) throw new NotificationException("priority is required");

        int targets = (hasText(r.getRecipientId()) ? 1 : 0)
                + (r.getRecipientIds() != null && !r.getRecipientIds().isEmpty() ? 1 : 0)
                + (hasText(r.getTopic()) ? 1 : 0);
        if (targets != 1) {
            throw new NotificationException(
                    "Exactly one recipient target is required: recipientId, recipientIds, or topic");
        }
        if (r.getRecipientIds() != null && !r.getRecipientIds().isEmpty()) {
            if (r.getRecipientIds().size() > 1000
                    || r.getRecipientIds().stream().anyMatch(id -> !hasText(id))) {
                throw new NotificationException("recipientIds must contain 1 to 1000 non-blank IDs");
            }
            if (new HashSet<>(r.getRecipientIds()).size() != r.getRecipientIds().size()) {
                throw new NotificationException("recipientIds must not contain duplicates");
            }
        }
        if (hasText(r.getTopic())) {
            if (!TOPIC.matcher(r.getTopic()).matches()) {
                throw new NotificationException("Invalid Firebase topic name");
            }
            if (r.getChannel() != com.goBhutan.adminPanel.notification.enums.NotificationChannel.PUSH) {
                throw new NotificationException("Topic notifications support PUSH channel only");
            }
        }

        boolean template = hasText(r.getTemplateCode());
        if ((!hasText(r.getTitle()) || !hasText(r.getBody())) && !template) {
            throw new NotificationException("Provide title/body or templateCode");
        }
        if (!template && (r.getTitle().length() > 200 || r.getBody().length() > 2000)) {
            throw new NotificationException("Notification title or body is too long");
        }
        Instant now = Instant.now();
        if (r.getExpiresAt() != null && !r.getExpiresAt().isAfter(now)) {
            throw new NotificationException("expiresAt must be in the future");
        }
        if (r.getScheduledAt() != null && r.getExpiresAt() != null
                && !r.getExpiresAt().isAfter(r.getScheduledAt())) {
            throw new NotificationException("expiresAt must be after scheduledAt");
        }
        if (r.getData() != null) {
            if (r.getData().size() > 50) {
                throw new NotificationException("Notification data supports at most 50 entries");
            }
            int bytes = 0;
            r.getData().forEach((k, v) -> {
                if (!hasText(k) || v == null) {
                    throw new NotificationException("Notification data keys and values are required");
                }
                String key = k.toLowerCase();
                if (RESERVED_DATA_KEYS.contains(key) || key.startsWith("google")
                        || key.startsWith("gcm")) {
                    throw new NotificationException("Reserved FCM data key: " + k);
                }
                if (key.contains("password") || key.contains("access_token")
                        || key.contains("secret") || key.contains("card")
                        || key.contains("bank")) {
                    throw new NotificationException("Sensitive key is not allowed in notification data: " + k);
                }
            });
            for (Map.Entry<String, String> entry : r.getData().entrySet()) {
                bytes += entry.getKey().getBytes(StandardCharsets.UTF_8).length;
                bytes += entry.getValue().getBytes(StandardCharsets.UTF_8).length;
            }
            if (bytes > 3500) {
                throw new NotificationException("Notification data payload is too large");
            }
        }
    }

    public void validateRendered(
            String title, String body, Map<String, String> data, String topic) {
        if (!hasText(title) || !hasText(body)) {
            throw new NotificationException("Rendered notification title and body are required");
        }
        if (title.length() > 200 || body.length() > 2000) {
            throw new NotificationException("Rendered notification title or body is too long");
        }
        int bytes = title.getBytes(StandardCharsets.UTF_8).length
                + body.getBytes(StandardCharsets.UTF_8).length;
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                bytes += entry.getKey().getBytes(StandardCharsets.UTF_8).length;
                bytes += entry.getValue().getBytes(StandardCharsets.UTF_8).length;
            }
        }
        int limit = hasText(topic) ? 1800 : 3800;
        if (bytes > limit) {
            throw new NotificationException("Rendered Firebase payload is too large");
        }
    }

    private void requireText(String value, String field) {
        if (!hasText(value)) throw new NotificationException(field + " is required");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
