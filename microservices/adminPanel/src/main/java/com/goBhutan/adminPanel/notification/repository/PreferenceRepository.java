package com.goBhutan.adminPanel.notification.repository;

import com.goBhutan.adminPanel.notification.model.NotificationPreferenceRecord;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
@RequiredArgsConstructor
public class PreferenceRepository {
    public static final String COLLECTION = "notificationPreferences";
    private final Firestore firestore;

    public Optional<NotificationPreferenceRecord> find(String userId) {
        DocumentSnapshot snap = FirestoreSupport.await(
                firestore.collection(COLLECTION).document(userId).get());
        return snap.exists()
                ? Optional.ofNullable(snap.toObject(NotificationPreferenceRecord.class))
                : Optional.empty();
    }

    public NotificationPreferenceRecord save(NotificationPreferenceRecord record) {
        FirestoreSupport.await(firestore.collection(COLLECTION)
                .document(record.getUserId()).set(record, SetOptions.merge()));
        return record;
    }
}
