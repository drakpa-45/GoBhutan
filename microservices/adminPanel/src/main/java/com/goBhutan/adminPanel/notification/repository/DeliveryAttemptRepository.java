package com.goBhutan.adminPanel.notification.repository;

import com.goBhutan.adminPanel.notification.model.DeliveryAttemptRecord;
import com.goBhutan.adminPanel.notification.enums.DeliveryOutcome;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
@RequiredArgsConstructor
public class DeliveryAttemptRepository {
    public static final String COLLECTION = "deliveryAttempts";
    private final Firestore firestore;

    public void save(DeliveryAttemptRecord record) {
        FirestoreSupport.await(firestore.collection(COLLECTION)
                .document(record.getId()).set(record));
    }

    public boolean wasAccepted(String notificationId, String targetId) {
        QuerySnapshot snapshot = FirestoreSupport.await(
                firestore.collection(COLLECTION)
                        .whereEqualTo("notificationId", notificationId)
                        .whereEqualTo("deviceRecordId", targetId)
                        .whereEqualTo("outcome", DeliveryOutcome.ACCEPTED.name())
                        .limit(1)
                        .get());
        return !snapshot.isEmpty();
    }

    public Set<String> findAcceptedTargets(String notificationId) {
        QuerySnapshot snapshot = FirestoreSupport.await(
                firestore.collection(COLLECTION)
                        .whereEqualTo("notificationId", notificationId)
                        .whereEqualTo("outcome", DeliveryOutcome.ACCEPTED.name())
                        .get());
        return snapshot.toObjects(DeliveryAttemptRecord.class).stream()
                .map(DeliveryAttemptRecord::getDeviceRecordId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    public int deleteBefore(Instant cutoff, int limit) {
        QuerySnapshot snapshot = FirestoreSupport.await(firestore.collection(COLLECTION)
                .whereLessThan("attemptedAt", cutoff).limit(limit).get());
        if (snapshot.isEmpty()) return 0;
        WriteBatch batch = firestore.batch();
        snapshot.getDocuments().forEach(doc -> batch.delete(doc.getReference()));
        FirestoreSupport.await(batch.commit());
        return snapshot.size();
    }
}
