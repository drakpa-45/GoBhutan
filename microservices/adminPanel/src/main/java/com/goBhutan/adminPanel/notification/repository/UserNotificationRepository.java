package com.goBhutan.adminPanel.notification.repository;

import com.goBhutan.adminPanel.notification.enums.InboxState;
import com.goBhutan.adminPanel.notification.exception.NotFoundException;
import com.goBhutan.adminPanel.notification.model.UserNotificationRecord;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Repository
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
@RequiredArgsConstructor
public class UserNotificationRepository {
    public static final String COLLECTION = "userNotifications";
    private final Firestore firestore;

    public boolean saveIfAbsent(UserNotificationRecord record) {
        DocumentReference ref = firestore.collection(COLLECTION).document(record.getId());
        return FirestoreSupport.await(firestore.runTransaction(tx -> {
            DocumentSnapshot existing = tx.get(ref).get();
            if (existing.exists()) return false;
            tx.create(ref, record);
            return true;
        }));
    }

    public Optional<UserNotificationRecord> findOwned(String id, String userId) {
        DocumentSnapshot snap = FirestoreSupport.await(
                firestore.collection(COLLECTION).document(id).get());
        if (snap.exists()) {
            UserNotificationRecord r = snap.toObject(UserNotificationRecord.class);
            if (r != null && userId.equals(r.getRecipientId())) return Optional.of(r);
        }
        QuerySnapshot byEvent = FirestoreSupport.await(firestore.collection(COLLECTION)
                .whereEqualTo("notificationId", id)
                .whereEqualTo("recipientId", userId)
                .limit(1).get());
        return byEvent.isEmpty() ? Optional.empty()
                : Optional.ofNullable(byEvent.getDocuments().get(0)
                        .toObject(UserNotificationRecord.class));
    }

    public List<UserNotificationRecord> list(
            String userId, InboxState state, String category, int limit) {
        Query q = firestore.collection(COLLECTION)
                .whereEqualTo("recipientId", userId);
        if (state != null) q = q.whereEqualTo("state", state.name());
        if (category != null && !category.isBlank()) q = q.whereEqualTo("category", category);
        q = q.orderBy("createdAt", Query.Direction.DESCENDING).limit(Math.min(limit, 100));
        return FirestoreSupport.await(q.get()).toObjects(UserNotificationRecord.class);
    }

    public long unreadCount(String userId) {
        Query q = firestore.collection(COLLECTION)
                .whereEqualTo("recipientId", userId)
                .whereEqualTo("state", InboxState.UNREAD.name());
        return FirestoreSupport.await(q.count().get()).getCount();
    }

    public void markRead(String id, String userId) {
        UserNotificationRecord record = findOwned(id, userId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        FirestoreSupport.await(firestore.collection(COLLECTION).document(record.getId())
                .update(Map.of("state", InboxState.READ.name(), "readAt", Instant.now())));
    }

    public void archive(String id, String userId) {
        UserNotificationRecord record = findOwned(id, userId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        FirestoreSupport.await(firestore.collection(COLLECTION).document(record.getId())
                .update(Map.of("state", InboxState.ARCHIVED.name(), "archivedAt", Instant.now())));
    }

    public void markAllRead(String userId) {
        Instant now = Instant.now();
        while (true) {
            QuerySnapshot qs = FirestoreSupport.await(
                    firestore.collection(COLLECTION)
                            .whereEqualTo("recipientId", userId)
                            .whereEqualTo("state", InboxState.UNREAD.name())
                            .limit(400).get());
            if (qs.isEmpty()) return;
            WriteBatch batch = firestore.batch();
            qs.getDocuments().forEach(doc ->
                    batch.update(doc.getReference(), Map.of(
                            "state", InboxState.READ.name(), "readAt", now)));
            FirestoreSupport.await(batch.commit());
            if (qs.size() < 400) return;
        }
    }

    public int deleteBefore(Instant cutoff, int limit) {
        QuerySnapshot snapshot = FirestoreSupport.await(firestore.collection(COLLECTION)
                .whereLessThan("createdAt", cutoff).limit(limit).get());
        if (snapshot.isEmpty()) return 0;
        WriteBatch batch = firestore.batch();
        snapshot.getDocuments().forEach(doc -> batch.delete(doc.getReference()));
        FirestoreSupport.await(batch.commit());
        return snapshot.size();
    }
}
