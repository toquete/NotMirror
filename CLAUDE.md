# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Install and run on connected device/emulator
./gradlew installDebug

# Build release APK
./gradlew assembleRelease
```

## Testing

```bash
# Run unit tests
./gradlew test

# Run a single unit test class
./gradlew :app:testDebugUnitTest --tests "com.toquete.notmirror.ExampleUnitTest"

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

## Lint & Code Quality

```bash
./gradlew lint
./gradlew :app:lintDebug
```

## Architecture

Android app (`app/`) + Electron desktop companion (`desktop/`). The Android app mirrors device notifications to the desktop over a local WebSocket connection.

- **Android flow**: `NotificationListenerService` → `NotificationWebSocketServer` (port 8765) → Electron client
- **Allowlist**: user-selected apps persisted via DataStore (`data/AllowlistDataStore.kt`); cached in-memory in the service
- **UI**: `MainActivity` → `MainViewModel` (StateFlow) → `SettingsScreen` (connection info card, permission banner, app list)
- **Theme**: `ui/theme/NotMirrorTheme` — dynamic color (Android 12+), falls back to static purple palette
- **Dependency versions**: managed centrally in `gradle/libs.versions.toml`; add deps there and reference via `libs.*` in `build.gradle.kts`
- **Min SDK**: 24 / **Target SDK**: 36 / **Kotlin**: 2.2.10 / **AGP**: 9.1.1 / **Compose BOM**: 2026.02.01

## Desktop app

```bash
cd desktop && npm install  # first time only
npm start                  # launch Electron app
```

## Compose gotchas

- **No extended icons**: `Icons.Default.ContentCopy` and most named icons require `material-icons-extended`, which is not in the base BOM. Use text buttons or add the dependency explicitly.
- **Clipboard**: `LocalClipboardManager` is deprecated. Use `LocalClipboard.current` + `rememberCoroutineScope()` and call `clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(...)))` from a launched coroutine.