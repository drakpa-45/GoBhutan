package com.goBhutan.adminPanel.notification.service;

import com.goBhutan.adminPanel.notification.dto.PreferenceRequest;
import com.goBhutan.adminPanel.notification.enums.NotificationCategory;
import com.goBhutan.adminPanel.notification.model.NotificationPreferenceRecord;
import com.goBhutan.adminPanel.notification.repository.PreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
@RequiredArgsConstructor
public class PreferenceService {
    private static final Set<NotificationCategory> MANDATORY = Set.of(
            NotificationCategory.PAYMENT_SUCCESS,
            NotificationCategory.PAYMENT_FAILED,
            NotificationCategory.SYSTEM_ALERT
    );
    private final PreferenceRepository repository;

    public NotificationPreferenceRecord get(String userId) {
        return repository.find(userId).orElse(NotificationPreferenceRecord.builder()
                .userId(userId).pushEnabled(true).disabledCategories(List.of()).build());
    }

    public NotificationPreferenceRecord update(String userId, PreferenceRequest request) {
        List<NotificationCategory> disabled = request.getDisabledCategories() == null
                ? List.of()
                : request.getDisabledCategories().stream()
                        .filter(c -> !MANDATORY.contains(c))
                        .sorted()
                        .toList();

        NotificationPreferenceRecord record = NotificationPreferenceRecord.builder()
                .userId(userId)
                .pushEnabled(request.getPushEnabled() == null || request.getPushEnabled())
                .disabledCategories(disabled)
                .updatedAt(Instant.now()).build();
        return repository.save(record);
    }
}
