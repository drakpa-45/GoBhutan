package com.goBhutan.adminPanel.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@NotificationModuleEnabled
@Slf4j
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp(NotificationProperties properties) throws Exception {
        String location = properties.getFirebase().getCredentialsLocation();
        if (location == null || location.isBlank()) {
            throw new IllegalStateException(
                    "Firebase credentials are required when the notification module is enabled. "
                            + "Set notification.firebase.credentials-location or "
                            + "NOTIFICATION_FIREBASE_CREDENTIALS_LOCATION.");
        }

        Path credentialsPath = Path.of(location).toAbsolutePath().normalize();
        if (!Files.isRegularFile(credentialsPath) || !Files.isReadable(credentialsPath)) {
            throw new IllegalStateException(
                    "Firebase credentials file does not exist or is not readable: " + credentialsPath);
        }

        GoogleCredentials credentials;
        try (FileInputStream in = new FileInputStream(credentialsPath.toFile())) {
            credentials = GoogleCredentials.fromStream(in);
        }
        FirebaseOptions.Builder builder = FirebaseOptions.builder().setCredentials(credentials);

        if (properties.getFirebase().getProjectId() != null
                && !properties.getFirebase().getProjectId().isBlank()) {
            builder.setProjectId(properties.getFirebase().getProjectId());
        }

        FirebaseApp app = FirebaseApp.getApps().stream()
                .findFirst()
                .orElseGet(() -> FirebaseApp.initializeApp(builder.build()));

        log.info("Firebase notification module initialized successfully projectId={} credentialsPath={}",
                properties.getFirebase().getProjectId(), credentialsPath);
        return app;
    }

    @Bean
    public Firestore firestore(NotificationProperties properties, FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(
            NotificationProperties properties,
            FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
