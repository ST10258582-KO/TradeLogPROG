# TradeLog

A professional trade journal Android application for active traders, built with Jetpack Compose, Firebase Authentication, and a Node.js REST API backend.

## Features

- **Google Sign-In (SSO)** — Secure authentication via Firebase + Credential Manager
- **Dashboard** — Live equity curve, win rate, net P&L, and key performance metrics
- **Trade Entry** — Log trades with instrument, direction, session, R-multiple, and custom tags
- **Trade List** — Filter by session (London / New York / Asia) and strategy tag
- **Analytics** — Session breakdown stats, tag performance, and equity curve visualisation
- **Journal** — Daily pre/post-market checklist with notes and historical entry browser
- **Settings** — Manage display name, timezone, default risk, and sign out

## Tech Stack

### Android App
- Kotlin + Jetpack Compose (single-activity MVVM)
- Firebase Authentication (Google Sign-In via Credential Manager)
- Retrofit 2 + OkHttp for REST API calls
- Kotlin Coroutines + StateFlow

### Backend API (`TradeLog-API/`)
- Node.js + Express + TypeScript
- Firebase Admin SDK for token verification
- Prisma ORM with SQLite
- Deployed target: Azure App Service

## Project Structure

```
TradeLog/
├── app/src/main/java/com/tradelog/app/
│   ├── data/
│   │   ├── model/          # Trade, User, Tag, JournalEntry data classes
│   │   ├── remote/         # Retrofit client + API service interface
│   │   └── repository/     # Auth, Trade, Settings repositories
│   ├── ui/
│   │   ├── navigation/     # Screen routes + NavHost
│   │   ├── screens/        # Auth, Dashboard, Trades, Analytics, Journal, Settings
│   │   └── theme/          # Material 3 colour/typography/shape tokens
│   └── viewmodel/          # AuthVM, TradeVM, AnalyticsVM, JournalVM, SettingsVM
└── app/src/main/res/       # Layouts, drawables, XML configs

TradeLog-API/
├── src/
│   ├── middleware/auth.ts  # Firebase token verification middleware
│   ├── routes/             # trades, analytics, tags, settings, auth
│   └── index.ts            # Express app entry point
└── prisma/schema.prisma    # Database schema (SQLite)
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Node.js 18+
- Firebase project with Google Sign-In enabled

### Running the Backend

```bash
cd TradeLog-API
npm install
npx prisma migrate dev --name init
npm run dev
```

API runs on `http://localhost:8080`. Health check: `GET /health`

### Running the Android App

1. Open `TradeLog/` in Android Studio
2. Create `google-services.json` from your Firebase project and place it in `app/`
3. Run on an emulator (API 26+) or physical device

### Running Tests

```bash
# Android unit tests
./gradlew test

# Android instrumented tests
./gradlew connectedAndroidTest
```

## CI/CD

GitHub Actions workflow at `.github/workflows/android-ci.yml` runs on every push:
- Builds debug APK
- Runs unit tests

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/health` | None | Health check |
| POST | `/auth/register` | Firebase | Register/upsert user |
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

## Student Details

- **Student:** Kent Orchard
- **Student Number:** ST10258582
- **Module:** PROG7314 / OPSC7312
- **Part:** 2
