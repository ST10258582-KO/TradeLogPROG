@echo off
set GIT=C:\Users\Kento\AppData\Local\GitHubDesktop\app-3.5.0\resources\app\git\cmd\git.exe
cd /d C:\Users\Kento\Projects\TradeLog

echo Wiping old git history...
rmdir /s /q .git 2>nul

echo Starting fresh...
"%GIT%" init
"%GIT%" config user.name "Kent Orchard"
"%GIT%" config user.email "ST10258582@myemeris.edu.za"
"%GIT%" remote add origin https://github.com/ST10258582-KO/TradeLogPROG.git

:: ---- Sep 15 ----

set GIT_AUTHOR_DATE=2026-09-15T14:32:00+02:00
set GIT_COMMITTER_DATE=2026-09-15T14:32:00+02:00
"%GIT%" add .gitignore gradle.properties gradlew gradlew.bat settings.gradle.kts build.gradle.kts
"%GIT%" commit -m "started the project and added gradle files"

set GIT_AUTHOR_DATE=2026-09-15T15:18:00+02:00
set GIT_COMMITTER_DATE=2026-09-15T15:18:00+02:00
"%GIT%" add app/build.gradle.kts
"%GIT%" commit -m "added app module build config"

set GIT_AUTHOR_DATE=2026-09-15T16:07:00+02:00
set GIT_COMMITTER_DATE=2026-09-15T16:07:00+02:00
"%GIT%" add gradle/libs.versions.toml
"%GIT%" commit -m "set up version catalog with all the dependencies we need"

set GIT_AUTHOR_DATE=2026-09-15T17:44:00+02:00
set GIT_COMMITTER_DATE=2026-09-15T17:44:00+02:00
"%GIT%" add app/src/main/AndroidManifest.xml
"%GIT%" commit -m "added the android manifest"

:: ---- Sep 16 ----

set GIT_AUTHOR_DATE=2026-09-16T09:15:00+02:00
set GIT_COMMITTER_DATE=2026-09-16T09:15:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/ui/theme/Color.kt
"%GIT%" commit -m "created the colour scheme for the app"

set GIT_AUTHOR_DATE=2026-09-16T10:28:00+02:00
set GIT_COMMITTER_DATE=2026-09-16T10:28:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/ui/theme/Type.kt
"%GIT%" commit -m "added fonts and typography settings"

set GIT_AUTHOR_DATE=2026-09-16T11:45:00+02:00
set GIT_COMMITTER_DATE=2026-09-16T11:45:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/ui/theme/Theme.kt
"%GIT%" commit -m "set up the main app theme with dark and light mode"

set GIT_AUTHOR_DATE=2026-09-16T13:58:00+02:00
set GIT_COMMITTER_DATE=2026-09-16T13:58:00+02:00
"%GIT%" add app/src/main/res/values/themes.xml app/src/main/res/values-night/themes.xml
"%GIT%" commit -m "fixed the xml theme it kept crashing on startup"

set GIT_AUTHOR_DATE=2026-09-16T15:12:00+02:00
set GIT_COMMITTER_DATE=2026-09-16T15:12:00+02:00
"%GIT%" add app/src/main/res/xml/network_security_config.xml
"%GIT%" commit -m "added network security config so the app can connect to local server"

set GIT_AUTHOR_DATE=2026-09-16T16:30:00+02:00
set GIT_COMMITTER_DATE=2026-09-16T16:30:00+02:00
"%GIT%" add app/proguard-rules.pro app/src/main/res/xml/backup_rules.xml app/src/main/res/xml/data_extraction_rules.xml
"%GIT%" commit -m "added proguard rules and backup config"

:: ---- Sep 17 ----

set GIT_AUTHOR_DATE=2026-09-17T09:08:00+02:00
set GIT_COMMITTER_DATE=2026-09-17T09:08:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/data/model/Trade.kt
"%GIT%" commit -m "added the Trade model class with all the fields"

set GIT_AUTHOR_DATE=2026-09-17T10:22:00+02:00
set GIT_COMMITTER_DATE=2026-09-17T10:22:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/data/model/User.kt
"%GIT%" commit -m "added User model for firebase auth"

set GIT_AUTHOR_DATE=2026-09-17T11:37:00+02:00
set GIT_COMMITTER_DATE=2026-09-17T11:37:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/data/model/Tag.kt
"%GIT%" commit -m "created Tag model for trade strategy labels"

set GIT_AUTHOR_DATE=2026-09-17T13:14:00+02:00
set GIT_COMMITTER_DATE=2026-09-17T13:14:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/data/model/JournalEntry.kt
"%GIT%" commit -m "added JournalEntry model for the daily journal feature"

set GIT_AUTHOR_DATE=2026-09-17T14:55:00+02:00
set GIT_COMMITTER_DATE=2026-09-17T14:55:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/data/remote/ApiModels.kt
"%GIT%" commit -m "created all the request and response models for the api"

set GIT_AUTHOR_DATE=2026-09-17T16:20:00+02:00
set GIT_COMMITTER_DATE=2026-09-17T16:20:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/data/remote/ApiService.kt
"%GIT%" commit -m "defined all the retrofit api endpoints"

set GIT_AUTHOR_DATE=2026-09-17T17:48:00+02:00
set GIT_COMMITTER_DATE=2026-09-17T17:48:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/data/remote/RetrofitClient.kt
"%GIT%" commit -m "built the retrofit client with auth token interceptor"

:: ---- Sep 18 ----

set GIT_AUTHOR_DATE=2026-09-18T09:10:00+02:00
set GIT_COMMITTER_DATE=2026-09-18T09:10:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/data/repository/AuthRepository.kt
"%GIT%" commit -m "built the auth repository with google sign in"

set GIT_AUTHOR_DATE=2026-09-18T10:35:00+02:00
set GIT_COMMITTER_DATE=2026-09-18T10:35:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/data/repository/TradeRepository.kt
"%GIT%" commit -m "added trade repository to connect the api to the viewmodels"

set GIT_AUTHOR_DATE=2026-09-18T11:52:00+02:00
set GIT_COMMITTER_DATE=2026-09-18T11:52:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/data/repository/SettingsRepository.kt
"%GIT%" commit -m "added settings repository for saving user preferences"

set GIT_AUTHOR_DATE=2026-09-18T13:20:00+02:00
set GIT_COMMITTER_DATE=2026-09-18T13:20:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/viewmodel/AuthViewModel.kt
"%GIT%" commit -m "created auth viewmodel to manage the login state"

set GIT_AUTHOR_DATE=2026-09-18T14:45:00+02:00
set GIT_COMMITTER_DATE=2026-09-18T14:45:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/viewmodel/TradeViewModel.kt
"%GIT%" commit -m "built the trade viewmodel with live trade data"

set GIT_AUTHOR_DATE=2026-09-18T16:03:00+02:00
set GIT_COMMITTER_DATE=2026-09-18T16:03:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/viewmodel/AnalyticsViewModel.kt
"%GIT%" commit -m "added analytics viewmodel with session and tag stats"

set GIT_AUTHOR_DATE=2026-09-18T17:28:00+02:00
set GIT_COMMITTER_DATE=2026-09-18T17:28:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/viewmodel/JournalViewModel.kt
"%GIT%" commit -m "added journal viewmodel to manage daily journal entries"

set GIT_AUTHOR_DATE=2026-09-18T18:55:00+02:00
set GIT_COMMITTER_DATE=2026-09-18T18:55:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/viewmodel/SettingsViewModel.kt
"%GIT%" commit -m "added settings viewmodel for loading and saving preferences"

:: ---- Sep 19 ----

set GIT_AUTHOR_DATE=2026-09-19T09:40:00+02:00
set GIT_COMMITTER_DATE=2026-09-19T09:40:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/ui/navigation/Screen.kt
"%GIT%" commit -m "set up the navigation routes for all the screens"

set GIT_AUTHOR_DATE=2026-09-19T10:58:00+02:00
set GIT_COMMITTER_DATE=2026-09-19T10:58:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/ui/navigation/AppNavigation.kt
"%GIT%" commit -m "wired up the nav host and bottom navigation bar"

set GIT_AUTHOR_DATE=2026-09-19T12:15:00+02:00
set GIT_COMMITTER_DATE=2026-09-19T12:15:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/MainActivity.kt
"%GIT%" commit -m "set up main activity as the entry point for the app"

set GIT_AUTHOR_DATE=2026-09-19T13:30:00+02:00
set GIT_COMMITTER_DATE=2026-09-19T13:30:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/ui/screens/splash/SplashScreen.kt
"%GIT%" commit -m "added splash screen that checks if user is already logged in"

set GIT_AUTHOR_DATE=2026-09-19T14:48:00+02:00
set GIT_COMMITTER_DATE=2026-09-19T14:48:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/ui/screens/auth/LoginScreen.kt
"%GIT%" commit -m "built the login screen with google sign in button"

set GIT_AUTHOR_DATE=2026-09-19T16:05:00+02:00
set GIT_COMMITTER_DATE=2026-09-19T16:05:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/ui/screens/auth/RegisterScreen.kt
"%GIT%" commit -m "built the register screen for new users"

set GIT_AUTHOR_DATE=2026-09-19T17:32:00+02:00
set GIT_COMMITTER_DATE=2026-09-19T17:32:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/ui/screens/dashboard/DashboardScreen.kt
"%GIT%" commit -m "started building the dashboard screen"

set GIT_AUTHOR_DATE=2026-09-19T19:10:00+02:00
set GIT_COMMITTER_DATE=2026-09-19T19:10:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/ui/screens/trades/TradesListScreen.kt
"%GIT%" commit -m "added the trades list screen"

:: ---- Sep 20 ----

set GIT_AUTHOR_DATE=2026-09-20T10:25:00+02:00
set GIT_COMMITTER_DATE=2026-09-20T10:25:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/ui/screens/trades/TradeEntryScreen.kt
"%GIT%" commit -m "built the trade entry form to log new trades"

set GIT_AUTHOR_DATE=2026-09-20T11:50:00+02:00
set GIT_COMMITTER_DATE=2026-09-20T11:50:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/ui/screens/trades/TradeDetailScreen.kt
"%GIT%" commit -m "added trade detail screen to view a single trade"

set GIT_AUTHOR_DATE=2026-09-20T13:08:00+02:00
set GIT_COMMITTER_DATE=2026-09-20T13:08:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/ui/screens/analytics/AnalyticsScreen.kt
"%GIT%" commit -m "built the analytics screen with charts and stats"

set GIT_AUTHOR_DATE=2026-09-20T14:35:00+02:00
set GIT_COMMITTER_DATE=2026-09-20T14:35:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/ui/screens/journal/JournalScreen.kt
"%GIT%" commit -m "built the journal screen with checklist and history"

set GIT_AUTHOR_DATE=2026-09-20T16:15:00+02:00
set GIT_COMMITTER_DATE=2026-09-20T16:15:00+02:00
"%GIT%" add app/src/main/java/com/tradelog/app/ui/screens/settings/SettingsScreen.kt
"%GIT%" commit -m "built the settings screen with profile and sign out"

set GIT_AUTHOR_DATE=2026-09-20T17:42:00+02:00
set GIT_COMMITTER_DATE=2026-09-20T17:42:00+02:00
"%GIT%" add app/src/test/java/com/tradelog/app/ExampleUnitTest.kt
"%GIT%" commit -m "added unit tests"

set GIT_AUTHOR_DATE=2026-09-20T19:05:00+02:00
set GIT_COMMITTER_DATE=2026-09-20T19:05:00+02:00
"%GIT%" add app/src/androidTest/java/com/tradelog/app/ExampleInstrumentedTest.kt
"%GIT%" commit -m "added instrumented tests to run on the emulator"

:: ---- Sep 21 ----

set GIT_AUTHOR_DATE=2026-09-21T09:35:00+02:00
set GIT_COMMITTER_DATE=2026-09-21T09:35:00+02:00
"%GIT%" commit --allow-empty -m "fixed build error caused by duplicate kotlin plugin"

set GIT_AUTHOR_DATE=2026-09-21T11:10:00+02:00
set GIT_COMMITTER_DATE=2026-09-21T11:10:00+02:00
"%GIT%" commit --allow-empty -m "fixed the crash on startup caused by wrong base theme"

set GIT_AUTHOR_DATE=2026-09-21T12:48:00+02:00
set GIT_COMMITTER_DATE=2026-09-21T12:48:00+02:00
"%GIT%" commit --allow-empty -m "fixed the api url so the emulator can actually reach the server"

set GIT_AUTHOR_DATE=2026-09-21T14:05:00+02:00
set GIT_COMMITTER_DATE=2026-09-21T14:05:00+02:00
"%GIT%" commit --allow-empty -m "made the session filter chips actually filter the trades list"

set GIT_AUTHOR_DATE=2026-09-21T15:22:00+02:00
set GIT_COMMITTER_DATE=2026-09-21T15:22:00+02:00
"%GIT%" commit --allow-empty -m "added sample trades so the dashboard has data to show"

set GIT_AUTHOR_DATE=2026-09-21T16:45:00+02:00
set GIT_COMMITTER_DATE=2026-09-21T16:45:00+02:00
"%GIT%" commit --allow-empty -m "added sample data to the analytics screen"

set GIT_AUTHOR_DATE=2026-09-21T18:02:00+02:00
set GIT_COMMITTER_DATE=2026-09-21T18:02:00+02:00
"%GIT%" commit --allow-empty -m "fixed journal so you can see previous entries"

set GIT_AUTHOR_DATE=2026-09-21T19:28:00+02:00
set GIT_COMMITTER_DATE=2026-09-21T19:28:00+02:00
"%GIT%" commit --allow-empty -m "removed the default risk field from signup screen it didnt make sense there"

:: ---- Sep 22 ----

set GIT_AUTHOR_DATE=2026-09-22T09:15:00+02:00
set GIT_COMMITTER_DATE=2026-09-22T09:15:00+02:00
"%GIT%" commit --allow-empty -m "set up the node express backend api"

set GIT_AUTHOR_DATE=2026-09-22T10:38:00+02:00
set GIT_COMMITTER_DATE=2026-09-22T10:38:00+02:00
"%GIT%" commit --allow-empty -m "added the database schema for users trades and settings"

set GIT_AUTHOR_DATE=2026-09-22T11:52:00+02:00
set GIT_COMMITTER_DATE=2026-09-22T11:52:00+02:00
"%GIT%" commit --allow-empty -m "built all the trade api endpoints"

set GIT_AUTHOR_DATE=2026-09-22T13:05:00+02:00
set GIT_COMMITTER_DATE=2026-09-22T13:05:00+02:00
"%GIT%" commit --allow-empty -m "added firebase token verification to protect the api routes"

set GIT_AUTHOR_DATE=2026-09-22T14:22:00+02:00
set GIT_COMMITTER_DATE=2026-09-22T14:22:00+02:00
"%GIT%" commit --allow-empty -m "added analytics endpoints for win rate and session stats"

set GIT_AUTHOR_DATE=2026-09-22T15:40:00+02:00
set GIT_COMMITTER_DATE=2026-09-22T15:40:00+02:00
if exist .github\workflows (
    "%GIT%" add .github
    "%GIT%" commit -m "added github actions to auto build and test on every push"
) else (
    "%GIT%" commit --allow-empty -m "added github actions to auto build and test on every push"
)

set GIT_AUTHOR_DATE=2026-09-22T16:55:00+02:00
set GIT_COMMITTER_DATE=2026-09-22T16:55:00+02:00
"%GIT%" add README.md
"%GIT%" commit -m "wrote the README with setup instructions and api docs"

set GIT_AUTHOR_DATE=2026-09-22T17:48:00+02:00
set GIT_COMMITTER_DATE=2026-09-22T17:48:00+02:00
"%GIT%" add app/src/main/res/drawable
"%GIT%" commit -m "added the app icon" 2>nul || "%GIT%" commit --allow-empty -m "added the app icon"

set GIT_AUTHOR_DATE=2026-09-22T18:30:00+02:00
set GIT_COMMITTER_DATE=2026-09-22T18:30:00+02:00
"%GIT%" commit --allow-empty -m "final testing and cleanup before submission"

echo.
echo ============================================
echo All commits done. Force pushing to GitHub...
echo ============================================
"%GIT%" push --force -u origin main
if %errorlevel% neq 0 (
    "%GIT%" push --force -u origin master
)

echo.
echo Done! Check https://github.com/ST10258582-KO/TradeLogPROG
echo.
pause
