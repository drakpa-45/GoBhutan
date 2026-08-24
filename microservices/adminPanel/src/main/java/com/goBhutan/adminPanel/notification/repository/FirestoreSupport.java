package com.goBhutan.adminPanel.notification.repository;

import com.goBhutan.adminPanel.notification.exception.NotificationException;
import com.google.api.core.ApiFuture;
import java.util.concurrent.TimeUnit;

public final class FirestoreSupport {
    private FirestoreSupport() {}
    public static <T> T await(ApiFuture<T> future) {
        try {
            return future.get(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new NotificationException("Firestore operation failed", e);
        }
    }
}
