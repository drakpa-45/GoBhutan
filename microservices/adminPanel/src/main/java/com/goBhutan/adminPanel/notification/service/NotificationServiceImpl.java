package com.goBhutan.adminPanel.notification.service;

import com.goBhutan.adminPanel.notification.dto.*;
import com.goBhutan.adminPanel.notification.enums.NotificationStatus;
import com.goBhutan.adminPanel.notification.exception.*;
import com.goBhutan.adminPanel.notification.model.NotificationEventRecord;
import com.goBhutan.adminPanel.notification.repository.NotificationEventRepository;
import com.goBhutan.adminPanel.notification.template.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRequestValidator validator;
    private final NotificationEventRepository repository;
    private final NotificationProcessor processor;
    private final TemplateService templateService;

    @Override
    public NotificationResult send(NotificationRequest request) {
        return create(request, false);
    }

    @Override
    public NotificationResult schedule(NotificationRequest request) {
        if (request == null) throw new NotificationException("Notification request is required");
        if (request.getScheduledAt() == null || !request.getScheduledAt().isAfter(Instant.now())) {
            throw new NotificationException("scheduledAt must be in the future");
        }
        return create(request, true);
    }

    private NotificationResult create(NotificationRequest request, boolean scheduled) {
        validator.validate(request);

        String title = request.getTitle();
        String body = request.getBody();
        if (request.getTemplateCode() != null) {
            TemplateService.Rendered rendered =
                    templateService.render(request.getTemplateCode(), request.getTemplateVariables());
            title = rendered.title();
            body = rendered.body();
        }
        validator.validateRendered(title, body, request.getData(), request.getTopic());

        List<String> recipients = request.getRecipientIds() != null
                ? new ArrayList<>(new LinkedHashSet<>(request.getRecipientIds()))
                : request.getRecipientId() != null ? List.of(request.getRecipientId()) : List.of();

        Instant now = Instant.now();
        NotificationEventRecord event = NotificationEventRecord.builder()
                .notificationId(UUID.randomUUID().toString())
                .eventId(request.getEventId())
                .recipientIds(recipients)
                .topic(request.getTopic())
                .title(title).body(body)
                .category(request.getCategory())
                .channel(request.getChannel())
                .sourceModule(request.getSourceModule())
                .sourceEntityType(request.getSourceEntityType())
                .sourceEntityId(request.getSourceEntityId())
                .actionType(request.getActionType())
                .actionValue(request.getActionValue())
                .imageUrl(request.getImageUrl())
                .priority(request.getPriority())
                .data(request.getData() == null ? Map.of() : request.getData())
                .scheduledAt(scheduled ? request.getScheduledAt() : null)
                .expiresAt(request.getExpiresAt())
                .createdAt(now)
                .status(scheduled ? NotificationStatus.SCHEDULED : NotificationStatus.PENDING)
                .retryCount(0)
                .nextAttemptAt(scheduled ? request.getScheduledAt() : now)
                .templateCode(request.getTemplateCode())
                .build();

        NotificationEventRepository.CreateResult created = repository.createIdempotent(event);
        NotificationEventRecord persisted = created.event();

        if (!created.duplicate() && !scheduled) {
            processor.processAsync(persisted);
        }

        return NotificationResult.builder()
                .notificationId(persisted.getNotificationId())
                .eventId(persisted.getEventId())
                .status(persisted.getStatus())
                .duplicate(created.duplicate())
                .recipientCount(persisted.getRecipientIds() == null ? 0 : persisted.getRecipientIds().size())
                .message(created.duplicate() ? "Duplicate event; existing notification returned" : "Accepted")
                .build();
    }

    @Override
    public void cancel(String notificationId) {
        repository.cancel(notificationId);
    }

    @Override
    public void retry(String notificationId) {
        NotificationEventRecord event = repository.prepareRetry(notificationId);
        processor.processAsync(event);
    }
}
