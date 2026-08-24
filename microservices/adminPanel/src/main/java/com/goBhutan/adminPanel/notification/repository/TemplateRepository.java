package com.goBhutan.adminPanel.notification.repository;

import com.goBhutan.adminPanel.notification.model.NotificationTemplateRecord;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
@RequiredArgsConstructor
public class TemplateRepository {
    public static final String COLLECTION = "notificationTemplates";
    private final Firestore firestore;

    public Optional<NotificationTemplateRecord> findActive(String code) {
        DocumentSnapshot snap = FirestoreSupport.await(
                firestore.collection(COLLECTION).document(code).get());
        if (!snap.exists()) return Optional.empty();
        NotificationTemplateRecord t = snap.toObject(NotificationTemplateRecord.class);
        return t != null && Boolean.TRUE.equals(t.getActive()) ? Optional.of(t) : Optional.empty();
    }
}
