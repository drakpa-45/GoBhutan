package com.goBhutan.adminPanel.notification.repository;

import com.goBhutan.adminPanel.notification.model.DeviceTokenRecord;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Repository
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
@RequiredArgsConstructor
public class DeviceTokenRepository {
    public static final String COLLECTION = "deviceTokens";
    private final Firestore firestore;

    public DeviceTokenRecord upsert(String userId, DeviceTokenRecord incoming) {
        String recordId = NotificationEventRepository.sha256(userId + "\u0000" + incoming.getDeviceId());
        DocumentReference ref = firestore.collection(COLLECTION).document(recordId);
        QuerySnapshot byToken = FirestoreSupport.await(
                firestore.collection(COLLECTION)
                        .whereEqualTo("firebaseToken", incoming.getFirebaseToken())
                        .get());

        Instant now = Instant.now();
        DeviceTokenRecord saved = FirestoreSupport.await(firestore.runTransaction(tx -> {
            DocumentSnapshot existing = tx.get(ref).get();
            DeviceTokenRecord record = incoming;
            record.setId(recordId);
            record.setUserId(userId);
            record.setActive(!Boolean.FALSE.equals(incoming.getPermissionGranted()));
            DeviceTokenRecord previous = existing.exists()
                    ? existing.toObject(DeviceTokenRecord.class) : null;
            boolean sameToken = previous != null
                    && Objects.equals(previous.getFirebaseToken(), incoming.getFirebaseToken());
            record.setTokenCreatedAt(sameToken && previous.getTokenCreatedAt() != null
                    ? previous.getTokenCreatedAt() : now);
            record.setTokenUpdatedAt(now);
            record.setLastSeenAt(now);
            tx.set(ref, record);
            return record;
        }));

        WriteBatch cleanup = firestore.batch();
        boolean hasCleanup = false;
        for (QueryDocumentSnapshot document : byToken.getDocuments()) {
            if (!document.getId().equals(recordId)) {
                cleanup.update(document.getReference(), Map.of(
                        "active", false, "tokenUpdatedAt", now));
                hasCleanup = true;
            }
        }
        if (hasCleanup) FirestoreSupport.await(cleanup.commit());
        return saved;
    }

    public List<DeviceTokenRecord> findActiveByUserId(String userId) {
        QuerySnapshot qs = FirestoreSupport.await(
                firestore.collection(COLLECTION)
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("active", true).get());
        return qs.toObjects(DeviceTokenRecord.class);
    }

    public Optional<DeviceTokenRecord> findByDeviceIdAndUser(String deviceId, String userId) {
        String id = NotificationEventRepository.sha256(userId + "\u0000" + deviceId);
        DocumentSnapshot snapshot = FirestoreSupport.await(
                firestore.collection(COLLECTION).document(id).get());
        return snapshot.exists()
                ? Optional.ofNullable(snapshot.toObject(DeviceTokenRecord.class))
                : Optional.empty();
    }

    public void deactivate(String recordId) {
        FirestoreSupport.await(firestore.collection(COLLECTION).document(recordId)
                .update(Map.of("active", false, "tokenUpdatedAt", Instant.now())));
    }

    public void deactivateByDeviceId(String deviceId, String userId) {
        findByDeviceIdAndUser(deviceId, userId).ifPresent(r -> deactivate(r.getId()));
    }
}
