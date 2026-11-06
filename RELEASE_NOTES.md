# TradeLog — Release Notes

---

v2.0 — Final Release (November 2026)

This release builds on the Part 2 prototype and introduces all advanced features
for the final submission. The app has been fully tested and runs on Android 8.0+.


New Features

Biometric Authentication
Fingerprint and face unlock added to the login screen using the AndroidX Biometric
library. Can be toggled in Settings. Falls back to Google Sign-In if the device
does not support biometrics or no credentials are enrolled.

Offline Mode with Local Database
Room database added as an offline-first local store. All trades and journal entries
are saved locally immediately so the app works without a network connection. Each
record carries a pending sync flag that is cleared once it has been uploaded.

Background Sync
A WorkManager SyncWorker runs every 15 minutes when the device has network access.
It picks up any pending local records and pushes them to Firestore automatically.
An offline banner is shown on the dashboard when no connection is detected.

Real-time Push Notifications
Firebase Cloud Messaging implemented with a custom service class. Two notification
channels are created at startup — Trade Alerts (high priority) and Journal Reminders
(default priority). Topic subscriptions are managed via the NotificationRepository.

Multi-Language Support
The app now supports English, Afrikaans, and Zulu. All 98 UI strings are translated
and stored in the appropriate resource directories. Language can be changed from
the Settings screen and persists across sessions.

Trade Screenshot Upload
Firebase Cloud Storage integrated via StorageRepository. Users can attach a chart
screenshot to any trade. Files are stored under users/{uid}/trades/{id}/ and the
download URL is saved with the trade record.

Position Risk Calculator
A dedicated offline screen for calculating position size, dollar risk, and
reward-to-risk ratio from account balance, stop loss, pip value, and take profit
inputs. No network connection required.

NoSQL Cloud Database
FirestoreRepository added for real-time reading and writing of trades and journal
entries. This is the cloud sync target for the offline Room database.


Backend

A custom REST API (Node.js, Express, TypeScript, Prisma, SQLite) is deployed and
live at https://tradelog-api-17ra.onrender.com. All endpoints require a valid
Firebase ID token. The API handles trade CRUD, analytics, tag management, and
user settings.


Bug Fixes

- Trade list not refreshing after a new trade was added
- Biometric prompt crashing on certain devices due to incorrect FragmentActivity reference
- Language preference not persisting after app restart
- Offline banner not showing when network dropped mid-session
- Notifications not displaying on Android 13 due to missing POST_NOTIFICATIONS permission

---

v1.0 — Part 2 Prototype (September 2026)

Initial prototype included Google Sign-In, dashboard, trade entry and list screens,
analytics, daily journal, settings, a local Node.js backend, and GitHub Actions CI/CD.
