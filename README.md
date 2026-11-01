# TradeLog — PROG7314 / OPSC7312 Final POE

> A production-ready Android trade journaling application for active retail traders.

**Student:** Kent Orchard | **Student Number:** ST10258582 | **Module:** PROG7314 / OPSC7312 | **Submission:** Final POE

---

## Demonstration Video

> [Demo Video — YouTube Unlisted](https://youtu.be/PLACEHOLDER_LINK)
>
> *The video demonstrates all features running live on a physical Android device.*

---

## Features

### Core Features (Part 2)
| Feature | Description |
|---------|-------------|
| **Google Sign-In (SSO)** | Firebase Authentication with Credential Manager |
| **Dashboard** | Live equity curve, win rate, net P&L, key metrics |
| **Trade Entry** | Log trades with instrument, direction, session, R-multiple, tags |
| **Trade List** | Filter by session and strategy tag |
| **Analytics** | Session breakdown, tag performance, equity curve visualisation |
| **Journal** | Daily pre/post-market checklist with notes |
| **Settings** | Display name, timezone, risk, currency, language, notifications |

### Advanced Features (Final POE)
| Feature | Implementation |
|---------|---------------|
| **Biometric Authentication** | AndroidX BiometricPrompt — fingerprint & face unlock before Dashboard access |
| **Offline Mode + Sync** | Room DB as local cache; WorkManager SyncWorker uploads pending changes to Firestore every 15 min when connected |
| **Real-time Notifications** | Firebase Cloud Messaging (FCM) with two notification channels: Trade Alerts (high priority) and Journal Reminders |
| **Multi-Language Support** | English (default), Afrikaans (`values-af/`), Zulu (`values-zu/`) — switchable from Settings |
| **Trade Screenshot Upload** | Firebase Cloud Storage — attach a chart screenshot (camera or gallery) to any trade entry |
| **Position Risk Calculator** | Offline-capable calculator: input account balance, risk %, stop size → outputs exact position size and dollar risk |

---

## Tech Stack

### Android Application
| Component | Library / API |
|-----------|-------------|
| Language | Kotlin 2.1.20 |
| UI Framework | Jetpack Compose (Material 3) |
| Architecture | MVVM + single-activity |
| Navigation | Jetpack Navigation Compose |
| Local Database | Room 2.7.1 (KSP annotation processing) |
| Cloud Database | Firebase Firestore (NoSQL) |
| Blob Storage | Firebase Cloud Storage |
| Push Notifications | Firebase Cloud Messaging (FCM) |
| Authentication | Firebase Auth + AndroidX Credentials Manager |
| Biometrics | AndroidX Biometric 1.1.0 |
| Background Sync | WorkManager 2.10.1 |
| Image Loading | Coil 2.7.0 |
| Networking | Retrofit 2.11 + OkHttp 4.12 |
| Settings | Jetpack DataStore Preferences |
| Async | Kotlin Coroutines + StateFlow |

### Backend API
| Component | Technology |
|-----------|-----------|
| Runtime | Node.js 20 + TypeScript |
| Framework | Express |
| Auth middleware | Firebase Admin SDK |
| ORM | Prisma |
| Database | SQLite (dev) / PostgreSQL (prod) |
| Hosting | Azure App Service |

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Android App                        │
│                                                      │
│  UI Layer (Compose Screens)                          │
│    ↕ StateFlow / collectAsState()                   │
│  ViewModel Layer (MVVM)                              │
│    ↕ suspend functions                              │
│  Repository Layer                                    │
│  ┌────────────┐  ┌─────────────┐  ┌──────────────┐ │
│  │ Room DB    │  │  Firestore  │  │  REST API    │ │
│  │ (offline)  │←→│  (NoSQL)    │  │  (Retrofit)  │ │
│  └────────────┘  └─────────────┘  └──────────────┘ │
│       ↑                                              │
│  SyncWorker (WorkManager — runs every 15 min)        │
└─────────────────────────────────────────────────────┘
         ↓ Firebase Auth token
┌─────────────────────────────────────────────────────┐
│              Backend API (Azure)                     │
│  Express + Prisma + Firebase Admin                   │
└─────────────────────────────────────────────────────┘
```

**Offline-first flow:**
1. User creates/edits trade → saved to Room DB immediately (UI updates instantly)
2. `isPendingSync = true` flagged on the entity
3. SyncWorker wakes when network is available → uploads pending entities to Firestore
4. On app start, Firestore data is pulled down and merged into Room DB

---

## Project Structure

```
TradeLog/
├── app/src/main/java/com/tradelog/app/
│   ├── data/
│   │   ├── local/              # Room DB — AppDatabase, DAOs, Entities
│   │   ├── model/              # Domain models (Trade, User, etc.)
│   │   ├── remote/             # Retrofit client + API service interface
│   │   └── repository/         # Auth, Trade, Settings, Firestore, Storage,
│   │                           # Biometric, Notification repositories
│   ├── services/               # Firebase Cloud Messaging service
│   ├── ui/
│   │   ├── navigation/         # Screen routes + NavHost
│   │   ├── screens/            # Auth, Dashboard, Trades, Analytics, Journal,
│   │   │                       # Settings, RiskCalculator screens
│   │   └── theme/              # Material 3 colour/typography tokens
│   ├── viewmodel/              # AuthVM, TradeVM, AnalyticsVM, JournalVM, SettingsVM
│   ├── workers/                # SyncWorker (WorkManager)
│   └── TradeLogApplication.kt  # Application class — channels, WorkManager init
└── app/src/main/res/
    ├── values/        # English strings (default)
    ├── values-af/     # Afrikaans strings
    └── values-zu/     # Zulu strings
```

---

## Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2) or later
- JDK 17
- Node.js 20+
- Firebase project (TradeLog) with:
  - Google Sign-In enabled
  - Firestore database (production mode)
  - Storage bucket enabled
  - Cloud Messaging enabled
  - `google-services.json` downloaded to `app/`

### Running the Backend

```bash
cd TradeLog-API
npm install
npx prisma migrate dev --name init
npm run dev
```

API runs on `http://localhost:8080`.

### Running the Android App

1. Open `TradeLog/` in Android Studio
2. Place `google-services.json` in `app/` folder
3. Build and run on a physical device (API 26+)

### Running Tests

```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests (device required)
```

---

## CI/CD — GitHub Actions

Workflow at `.github/workflows/android-ci.yml` triggers on every push to `main`/`develop`:

1. **Checkout** source
2. **Set up JDK 17** (Temurin distribution)
3. **Run unit tests** (`./gradlew test`)
4. **Build debug APK** (`./gradlew assembleDebug`)
5. **Upload test results** as build artifacts
6. **Upload debug APK** as build artifact

---

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/health` | None | Health check |
| POST | `/auth/register` | Firebase | Register / upsert user profile |
| GET | `/trades` | Required | List user's trades |
| POST | `/trades` | Required | Create a trade |
| GET | `/trades/:id` | Required | Get single trade |
| PUT | `/trades/:id` | Required | Update trade |
| DELETE | `/trades/:id` | Required | Delete trade |
| GET | `/analytics/summary` | Required | Win rate, net P&L, avg R |
| GET | `/analytics/sessions` | Required | Per-session breakdown |
| GET | `/tags` | Required | List tags |
| GET | `/settings` | Required | Get user settings |
| PUT | `/settings` | Required | Update settings |

---

## Release Notes

### v2.0 — Final POE Submission (November 2026)

**New Advanced Features:**
- **Biometric Authentication** — Fingerprint and face unlock using AndroidX BiometricPrompt API. Enable in Settings. Falls back to Google Sign-In if biometrics unavailable.
- **Offline Mode with Sync** — Room DB caches all trade data locally. WorkManager SyncWorker runs every 15 minutes when connected and uploads any pending changes to Firestore. The offline banner is displayed when connectivity is lost.
- **Real-time Push Notifications** — Firebase Cloud Messaging integration. Two channels: Trade Alerts (high-priority) and Journal Reminders (default). Users can subscribe/unsubscribe via Settings.
- **Multi-Language Support** — Application fully localised in English (default), Afrikaans, and Zulu. Language selection in Settings immediately applies throughout the app.
- **Trade Screenshot Upload (Blob Storage)** — Attach a chart screenshot from camera or gallery to any trade. Images are uploaded to Firebase Cloud Storage under `users/{uid}/trades/{tradeId}/`. Displayed inline in Trade Detail screen.
- **Position Risk Calculator** — Works fully offline. Input account balance, risk %, stop size in pips/points and pip value → app calculates exact position size (lots/units), dollar risk, and risk:reward ratio.

**Infrastructure:**
- Added NoSQL database layer (Firebase Firestore) mirroring local Room DB
- WorkManager background sync architecture
- Production app icon and signing configuration
- Version bumped to 2.0

### v1.0 — Part 2 Prototype (September 2026)
- Google Sign-In via Firebase Authentication
- Full MVVM architecture with Jetpack Compose
- Dashboard with equity curve and performance metrics
- Trade entry, list, detail, and filter screens
- Analytics screen with session breakdown
- Daily journal with pre/post-market notes
- Settings with DataStore persistence
- REST API backend (Node.js + Prisma)
- GitHub Actions CI/CD

---

## Play Store Preparation

- Production APK signed with release keystore
- App icon configured at all densities (mdpi → xxxhdpi)
- Minimum SDK: API 26 (Android 8.0) — covers 99%+ of active devices
- Target SDK: API 36
- ProGuard minification enabled for release builds
- Screenshots captured on physical device for Play Store listing

---

## AI Write-Up

**How AI tooling was utilised in this project:**

Generative AI (Claude by Anthropic) was used as a code-generation and debugging assistant throughout the development of the Final POE. Its use falls into three categories:

1. **Code generation** — Boilerplate code for Room entities/DAOs, the WorkManager SyncWorker, and the BiometricPrompt integration was generated with AI assistance. Each generated block was reviewed, adapted to match the existing project structure, and tested before inclusion. References to official documentation (Firebase, Android Developers) were verified independently.

2. **Bug fixing** — When the Firebase Credential Manager flow returned unexpected errors, AI was used to identify that the Google Web Client ID must come from the `google-services.json` OAuth client, not a manually entered value. The fix was verified by running the app.

3. **Documentation** — The README, Afrikaans and Zulu string translations, and inline code comments were drafted with AI assistance and then reviewed for accuracy. Zulu translations were cross-checked against the South African official language reference materials.

All AI-generated code is cited inline with comments referencing the source (e.g., "Reference: Google LLC, 'Room Persistence Library' Android Developers (2024)"). The core business logic, architecture decisions, and UI design were created by the student.

*Word count: ~220 words*
