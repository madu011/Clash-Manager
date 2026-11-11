# Clash Manager

**Designed & Developed by Madhuranga Yasanath**

An Android companion app for Clash of Clans village recovery planning. The project is being built in stages, starting with a native Android dashboard for builder timers, upgrade tracking, resource planning, and reminders.

## Current implementation

- Native Android app using Kotlin and Jetpack Compose.
- Local builder task tracking with countdowns and progress indicators.
- Add and complete upgrade tasks.
- Builder slots and laboratory task tracking.
- Local persistence on the device.
- Android notification permission and reminder foundation.
- GitHub Actions Android debug APK build.

## Important integration limits

The Supercell Clash of Clans API is read-only and requires a valid API token configured through a secure backend. Never commit API tokens or embed secrets in the APK. The API does not expose every live village detail or builder countdown. Missing information must be supplied by the user or supported screenshot/import workflows.

The app currently provides a local/manual tracker. Live account synchronization, AI recommendations, and shareable base-layout generation are planned integrations and are not represented as completed features.

## Build

Open this repository in Android Studio, or run the GitHub Actions workflow **Android Debug APK**. The workflow builds the debug APK and uploads it as an artifact.

## Developer

Madhuranga Yasanath