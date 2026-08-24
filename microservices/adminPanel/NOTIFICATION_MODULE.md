# Notification module deployment and integration

The notification module is disabled unless Firebase is explicitly enabled. This keeps the
host application startable in environments that do not provide Firebase.

## Runtime configuration

Prefer Application Default Credentials in hosted environments:

```text
NOTIFICATION_FIREBASE_ENABLED=true
NOTIFICATION_FIREBASE_PROJECT_ID=<firebase-project-id>
```

For local development, a service-account file can be supplied outside the repository:

```text
NOTIFICATION_FIREBASE_CREDENTIALS_LOCATION=<absolute-path-to-service-account-json>
```

Never commit the service-account JSON file.

For this project, the recommended local credential path is:

```text
D:/GoBhutanSecrets/firebase/yaya-d0e08-firebase-adminsdk-fbsvc-edc6296cf4.json
```

The repository `.gitignore` blocks common Firebase/Google credential filenames, but this is a
last line of defense only; private keys must still be stored outside the repository.

The `local` Spring profile is configured for project `yaya-d0e08` and the secured path above.
Start it with:

```text
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Other profiles remain disabled unless `NOTIFICATION_FIREBASE_ENABLED=true` is supplied.

Optional settings:

```yaml
notification:
  admin-role: notification_manage
  retry:
    max-attempts: 3
    initial-delay-seconds: 30
  scheduler:
    fixed-delay-ms: 15000
    lease-seconds: 120
    batch-size: 100
  async:
    core-pool-size: 4
    max-pool-size: 8
    queue-capacity: 500
  retention:
    notification-days: 90
    delivery-attempt-days: 30
    cron: "0 30 2 * * *"
    zone: Asia/Thimphu
```

Deploy `firestore.rules` and `firestore.indexes.json` to a dedicated notification Firebase
project, or merge them with that project's existing Firebase configuration before deployment.
The supplied rules intentionally deny all direct client access because the module is backend-only.

## Recipient identity contract

Device registration stores the authenticated Keycloak JWT `sub` value. Existing modules must
therefore use the same Keycloak subject (normally `AppUser.keycloakId`) as `recipientId`.
Do not mix application UUIDs, usernames, emails, and Keycloak subjects.

## Event IDs

`eventId` is the global idempotency key. It must be stable and unique across all modules:

```text
PAYMENT:PAYMENT_SUCCESS:<transaction-id>
HOTEL:BOOKING_CONFIRMED:<booking-id>
BUS:SCHEDULE_CHANGED:<schedule-id>:<version>
```

## Transaction integration

`NotificationAfterCommitPublisher` prevents notification failures from rolling back a completed
business transaction. It is best-effort between the SQL commit and the Firestore enqueue. If a
notification must be guaranteed after a database commit, integrate through a transactional SQL
outbox and relay the outbox record to `NotificationService`.

## Client integration

For `PUSH_AND_IN_APP`, FCM data includes both:

- `notificationId`: global notification event ID.
- `inboxId`: recipient-specific inbox document ID.

Inbox read/archive APIs accept either identifier for the authenticated recipient.

Browser clients use `PATCH` for read/archive operations. Add `PATCH` to the host application's
CORS allowed methods when integrating the module.

## Delivery semantics

Delivery is at-least-once. Retries skip targets that already have an accepted delivery attempt.
A process failure after FCM accepts a message but before the attempt is persisted can still cause
a duplicate; clients should treat `notificationId` as their deduplication key.
