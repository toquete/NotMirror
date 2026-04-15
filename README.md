# NotMirror

Mirror your Android notifications to your desktop in real time over a local Wi-Fi connection.

NotMirror consists of two parts:

- **Android app** — listens for notifications via `NotificationListenerService` and streams them over WebSocket
- **Electron desktop app** — connects to the Android device and surfaces notifications as native OS alerts

---

## How It Works

```
Android device                          Desktop
─────────────────────────────────────────────────
NotificationListenerService
        │
        ▼
NotificationWebSocketServer (port 8765) ──► Electron client
                                                   │
                                                   ▼
                                         Native OS notification
```

The Android app runs a foreground WebSocket server on port **8765**. The Electron client connects to it over the local network and displays incoming notifications. A user-managed allowlist controls which apps get forwarded.

---

## Requirements

| Component | Requirement |
|-----------|-------------|
| Android   | API 24 (Android 7.0) or higher |
| Desktop   | macOS, Windows, or Linux with Node.js |
| Network   | Both devices on the same local network |

---

## Getting Started

### Android

1. Build and install the app:
   ```bash
   ./gradlew installDebug
   ```
2. Open the app and grant **Notification Access** when prompted.
3. Select the apps whose notifications you want to mirror.
4. Note the IP address shown in the app — you'll need it for the desktop client.

### Desktop

1. Install dependencies (first time only):
   ```bash
   cd desktop && npm install
   ```
2. Launch the Electron app:
   ```bash
   npm start
   ```
3. Enter your Android device's IP address and click **Connect**.

---

## Development

### Build

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

### Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

### Lint

```bash
./gradlew lint
```

---

## Tech Stack

**Android**
- Kotlin 2.2.10 / AGP 9.1.1
- Jetpack Compose (BOM 2026.02.01) with Material 3
- DataStore for allowlist persistence
- `java-websocket` for the WebSocket server
- Kotlin Serialization for JSON payloads

**Desktop**
- Electron
- `ws` WebSocket client with auto-reconnect
