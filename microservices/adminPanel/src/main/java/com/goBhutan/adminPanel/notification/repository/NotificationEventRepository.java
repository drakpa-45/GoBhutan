package com.goBhutan.adminPanel.notification.repository;

import com.goBhutan.adminPanel.notification.enums.NotificationStatus;
import com.goBhutan.adminPanel.notification.model.NotificationEventRecord;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Repository
@com.goBhutan.adminPanel.notification.config.NotificationModuleEnabled
@RequiredArgsConstructor
public class NotificationEventRepository {
    public static final String COLLECTION = "notificationEvents";
    private final Firestore firestore;

    public record CreateResult(NotificationEventRecord event, boolean duplicate) {}

    public CreateResult createIdempotent(NotificationEventRecord event) {
        String eventKey = sha256(event.getEventId());
        DocumentReference ref = firestore.collection(COLLECTION).document(eventKey);

        return FirestoreSupport.await(firestore.runTransaction(tx -> {
            DocumentSnapshot existing = tx.get(ref).get();
            if (existing.exists()) {
                return new CreateResult(existing.toObject(NotificationEventRecord.class), true);
            }
            tx.create(ref, event);
            return new CreateResult(event, false);
        }));
    }

    public Optional<NotificationEventRecord> findByNotificationId(String notificationId) {
        QuerySnapshot qs = FirestoreSupport.await(
                firestore.collection(COLLECTION)
                        .whereEqualTo("notificationId", notificationId)
                        .limit(1).get());
        return qs.isEmpty() ? Optional.empty()
                : Optional.of(qs.getDocuments().get(0).toObject(NotificationEventRecord.class));
    }

    public List<NotificationEventRecord> findDue(int limit) {
        Instant now = Instant.now();
        Query pending = firestore.collection(COLLECTION)
                .whereIn("status", List.of(
                        NotificationStatus.PENDING.name(),
                        NotificationStatus.SCHEDULED.name()))
                .whereLessThanOrEqualTo("nextAttemptAt", now)
                .limit(limit);
        List<NotificationEventRecord> due = new ArrayList<>(
                FirestoreSupport.await(pending.get()).toObjects(NotificationEventRecord.class));
        if (due.size() < limit) {
            Query abandoned = firestore.collection(COLLECTION)
                    .whereEqualTo("status", NotificationStatus.PROCESSING.name())
                    .whereLessThanOrEqualTo("leaseUntil", now)
                    .limit(limit - due.size());
            due.addAll(FirestoreSupport.await(abandoned.get())
                    .toObjects(NotificationEventRecord.class));
        }
        return due;
    }

    public Optional<NotificationEventRecord> acquireLease(
            NotificationEventRecord event, long leaseSeconds) {
        DocumentReference ref = firestore.collection(COLLECTION).document(sha256(event.getEventId()));
        return FirestoreSupport.await(firestore.runTransaction(tx -> {
            DocumentSnapshot snap = tx.get(ref).get();
            if (!snap.exists()) return Optional.empty();
            NotificationEventRecord current = snap.toObject(NotificationEventRecord.class);
            if (current == null) return Optional.empty();
            Instant now = Instant.now();
            if (current.getLeaseUntil() != null && current.getLeaseUntil().isAfter(now)) {
                return Optional.empty();
            }
            if (current.getStatus() == NotificationStatus.CANCELLED
                    || current.getStatus() == NotificationStatus.SENT
                    || current.getStatus() == NotificationStatus.EXPIRED) {
                return Optional.empty();
            }
            String leaseId = UUID.randomUUID().toString();
            current.setStatus(NotificationStatus.PROCESSING);
            current.setLeaseUntil(now.plusSeconds(leaseSeconds));
            current.setLeaseId(leaseId);
            tx.update(ref, Map.of(
                    "status", NotificationStatus.PROCESSING.name(),
                    "leaseUntil", current.getLeaseUntil(),
                    "leaseId", leaseId
            ));
            return Optional.of(current);
        }));
    }

    public boolean completeIfLeaseOwned(NotificationEventRecord event) {
        DocumentReference ref = firestore.collection(COLLECTION).document(sha256(event.getEventId()));
        return FirestoreSupport.await(firestore.runTransaction(tx -> {
            DocumentSnapshot snap = tx.get(ref).get();
            if (!snap.exists()) return false;
            NotificationEventRecord current = snap.toObject(NotificationEventRecord.class);
            if (current == null
                    || current.getStatus() != NotificationStatus.PROCESSING
                    || !Objects.equals(current.getLeaseId(), event.getLeaseId())) {
                return false;
            }
            event.setLeaseUntil(null);
            event.setLeaseId(null);
            tx.set(ref, event, SetOptions.merge());
            return true;
        }));
    }

    public NotificationEventRecord cancel(String notificationId) {
        return changeState(notificationId, false);
    }

    public NotificationEventRecord prepareRetry(String notificationId) {
        return changeState(notificationId, true);
    }

    private NotificationEventRecord changeState(String notificationId, boolean retry) {
        NotificationEventRecord found = findByNotificationId(notificationId)
                .orElseThrow(() -> new com.goBhutan.adminPanel.notification.exception.NotFoundException(
                        "Notification not found"));
        DocumentReference ref = firestore.collection(COLLECTION)
                .document(sha256(found.getEventId()));
        return FirestoreSupport.await(firestore.runTransaction(tx -> {
            DocumentSnapshot snap = tx.get(ref).get();
            NotificationEventRecord current = snap.toObject(NotificationEventRecord.class);
            if (current == null) {
                throw new com.goBhutan.adminPanel.notification.exception.NotFoundException(
                        "Notification not found");
            }
            if (retry) {
                if (current.getStatus() != NotificationStatus.FAILED
                        && current.getStatus() != NotificationStatus.PARTIALLY_SENT) {
                    throw new com.goBhutan.adminPanel.notification.exception.NotificationException(
                            "Only failed or partially sent notifications can be retried");
                }
                current.setStatus(NotificationStatus.PENDING);
                current.setNextAttemptAt(Instant.now());
                current.setRetryCount(0);
                current.setFailureReason(null);
            } else {
                if (current.getStatus() == NotificationStatus.SENT
                        || current.getStatus() == NotificationStatus.EXPIRED
                        || current.getStatus() == NotificationStatus.CANCELLED) {
                    throw new com.goBhutan.adminPanel.notification.exception.NotificationException(
                            "Notification cannot be cancelled in state " + current.getStatus());
                }
                current.setStatus(NotificationStatus.CANCELLED);
            }
            current.setLeaseUntil(null);
            current.setLeaseId(null);
            tx.set(ref, current, SetOptions.merge());
            return current;
        }));
    }

    public void update(NotificationEventRecord event) {
        FirestoreSupport.await(firestore.collection(COLLECTION)
                .document(sha256(event.getEventId())).set(event, SetOptions.merge()));
    }

    public int deleteTerminalBefore(Instant cutoff, int limit) {
        QuerySnapshot snapshot = FirestoreSupport.await(firestore.collection(COLLECTION)
                .whereIn("status", List.of(
                        NotificationStatus.SENT.name(),
                        NotificationStatus.PARTIALLY_SENT.name(),
                        NotificationStatus.FAILED.name(),
                        NotificationStatus.CANCELLED.name(),
                        NotificationStatus.EXPIRED.name()))
                .whereLessThan("createdAt", cutoff)
                .limit(limit).get());
        return delete(snapshot);
    }

    private int delete(QuerySnapshot snapshot) {
        if (snapshot.isEmpty()) return 0;
        WriteBatch batch = firestore.batch();
        snapshot.getDocuments().forEach(doc -> batch.delete(doc.getReference()));
        FirestoreSupport.await(batch.commit());
        return snapshot.size();
    }

    public static String sha256(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
