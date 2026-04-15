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

Single-module Android app (`app/`) using Jetpack Compose with Material3. The project is a scaffold — currently only `MainActivity` exists with a basic Compose UI.

- **UI entry point**: `MainActivity` sets content via `setContent { NotMirrorTheme { ... } }`
- **Theme**: `ui/theme/` — `NotMirrorTheme` supports dynamic color (Android 12+), dark/light schemes, and falls back to static purple-based palette
- **Dependency versions**: managed centrally in `gradle/libs.versions.toml` (version catalog); add new deps there, reference via `libs.*` aliases in `build.gradle.kts`
- **Min SDK**: 24 / **Target SDK**: 36 / **Kotlin**: 2.2.10 / **AGP**: 9.1.1 / **Compose BOM**: 2026.02.01