# Android client

Kotlin, Jetpack Compose, Ktor Client (CIO engine), Koin, Navigation 3.

## Architecture

Packages are organized by feature: `ui/screens/signin/` holds its screen, view model, and contract together. Each screen is a `Contract` (sealed `State` / `Event` / `Effect`), a `ViewModel` that reduces events to state and emits one-shot effects, and a `Screen` composable that is a pure function of state.

```
domain/    Plain Kotlin. No androidx.credentials import.
data/      Network and local session storage.
platform/  Android edges: Activity, BackupAgent, and the Credential Manager gateway.
```

`domain/repository/RestoreCredentialGateway.kt` defines the interface (`register`, `signIn`, `clear`). `platform/credentials/AndroidRestoreCredentialGateway.kt` is its only implementation and the only file that imports `androidx.credentials`. Everything above that boundary (the two use cases, the view models, the screens) is plain Kotlin.

## Restore retrieval and process startup

Two call sites drive the restore attempt:

- `RestoreCredentialBackupAgent.onRestoreFinished()`, a background hook the system calls after app data is restored, before the app is opened.
- `SplashViewModel.resolveStartDestination()`, a foreground check on first launch, as a fallback.

`onRestoreFinished()` fired reliably in testing but crashed with `IllegalStateException: KoinApplication has not been started` before reaching the network. `BackupAgent` restore delivery uses a stripped-down process start where neither `Application.onCreate()` nor content providers (including App Startup's `InitializationProvider`) are guaranteed to have run, so DI may not be wired up yet. The fix, in `RestoreCredentialBackupAgent.onCreate()`:

```kotlin
override fun onCreate() {
    super.onCreate()
    AppInitializer.getInstance(this).initializeComponent(KoinInitializer::class.java)
}
```

`KoinInitializer` (`di/KoinInitializer.kt`) is the only place `startKoin { }` runs, and `initializeComponent(...)` is idempotent, so a normal launch and a restore-mode launch converge on the same Koin instance.

## Build config

`SERVER_BASE_URL` (`app/build.gradle.kts`) defaults to `http://127.0.0.1:8080`, used with `adb reverse tcp:8080 tcp:8080`. Works on the emulator and over USB on a physical device. Point it at a real HTTPS host to run the full ceremony; Credential Manager needs Digital Asset Links from a real domain (see the root README).

`network_security_config.xml` allows cleartext to `127.0.0.1` only; the app does not set `usesCleartextTraffic` globally.

## Running it

```bash
./gradlew :app:assembleDebug
adb install -r -t app/build/outputs/apk/debug/app-debug.apk
adb reverse tcp:8080 tcp:8080   # re-run whenever adb reconnects
```

See `TESTING.md` for exercising the create and restore ceremonies.
