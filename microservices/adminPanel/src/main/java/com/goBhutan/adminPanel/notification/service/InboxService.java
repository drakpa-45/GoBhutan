package com.goBhutan.adminPanel.notification.service;

import com.goBhutan.adminPanel.notification.enums.InboxState;
import com.goBhutan.adminPanel.notification.exception.NotFoundException;
import com.goBhutan.adminPanel.notification.model.UserNotificationRecord;
import com.goBhutan.adminPanel.notification.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
@RequiredArgsConstructor
public class InboxService {
    private final UserNotificationRepository repository;

    public List<UserNotificationRecord> list(String userId, InboxState state, String category, int limit) {
        return repository.list(userId, state, category, limit);
    }
    public UserNotificationRecord get(String userId, String id) {
        return repository.findOwned(id, userId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
    }
    public long unreadCount(String userId) { return repository.unreadCount(userId); }
    public void read(String userId, String id) {
        get(userId, id);
        repository.markRead(id, userId);
    }
    public void readAll(String userId) { repository.markAllRead(userId); }
    public void archive(String userId, String id) {
        get(userId, id);
        repository.archive(id, userId);
    }
}
