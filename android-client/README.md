# Android client

Kotlin, Jetpack Compose, Ktor Client with the **CIO** engine, Koin for DI, Navigation 3.

## Architecture

Packages are organized by feature, not by technical layer — `ui/screens/signin/` holds its screen, view model, and contract together, rather than splitting all screens into one folder and all view models into another. Each screen follows the same MVI-flavored shape: a `Contract` (sealed `State`/`Event`/`Effect`), a `ViewModel` that reduces events into state and emits one-shot effects, and a `Screen` composable that's a pure function of state.

The layering that actually matters for this repo:

```
domain/          Plain Kotlin. No androidx.credentials import anywhere in here.
data/            Network + local session storage.
platform/         The Android-specific edges: Activity, BackupAgent, and...
platform/credentials/AndroidRestoreCredentialGateway.kt   <- the ONLY file that imports androidx.credentials
```

`domain/repository/RestoreCredentialGateway.kt` defines the interface (`register`, `signIn`, `clear`); `AndroidRestoreCredentialGateway` is the only implementation, and it's the only place in the codebase that touches the Credential Manager API. Everything above that boundary — the two use cases that drive it (`RegisterRestoreCredentialUseCase`, `TryRestoreSignInUseCase`), the view models, the screens — is plain Kotlin with no Android Credential Manager dependency. Swapping that one file is what portability to a KMP target would mean, for whatever that's worth given Credential Manager itself is Android-only.

## Restore retrieval, and the bug that made it look flaky

Two call sites drive the restore attempt, matching Google's own guidance:

- `RestoreCredentialBackupAgent.onRestoreFinished()` — a background hook the system calls right after app data has been restored to a new device, before the user opens the app.
- `SplashViewModel.resolveStartDestination()` — a plain foreground check on first launch, as a fallback.

The background hook looked unreliable for a while: `onRestoreFinished()` fired every time (confirmed with logging), but crashed on `IllegalStateException: KoinApplication has not been started` before it ever reached the network. The cause: `BackupAgent` restore delivery uses a stripped-down process bring-up where **neither** `Application.onCreate()` **nor** content providers (including AndroidX App Startup's own `InitializationProvider`) are guaranteed to have run yet — so whichever of those you're relying on to wire up DI might simply not exist when `onRestoreFinished()` fires. The fix in `RestoreCredentialBackupAgent.onCreate()`:

```kotlin
override fun onCreate() {
    super.onCreate()
    AppInitializer.getInstance(this).initializeComponent(KoinInitializer::class.java)
}
```

`KoinInitializer` (an `androidx.startup.Initializer<Koin>`, in `di/KoinInitializer.kt`) is the single place `startKoin{...}` is called. `AppInitializer.getInstance(this).initializeComponent(...)` is idempotent, so a normal launch (where App Startup's provider already ran it) and a restore-mode launch (where nothing has run it yet) both converge on the same Koin instance, with no duplicated setup code and no defensive `GlobalContext.getOrNull()` checks scattered around.

## Build config

`SERVER_BASE_URL` (in `app/build.gradle.kts`) defaults to `http://127.0.0.1:8080`, paired with `adb reverse tcp:8080 tcp:8080` — more reliable across emulator network configurations than the classic `10.0.2.2` alias, and it works over USB on a physical device too. Point it at a real HTTPS host to exercise the actual restore-credential ceremony end to end (Credential Manager needs Digital Asset Links served from a real domain — see the root README).

`network_security_config.xml` carries a scoped cleartext exception for `127.0.0.1`/`10.0.2.2` only — cleartext traffic is blocked by default since API 28, and this app deliberately doesn't set `usesCleartextTraffic="true"` globally just to talk to a local dev server.

## Running it

```bash
./gradlew :app:assembleDebug
adb install -r -t app/build/outputs/apk/debug/app-debug.apk
adb reverse tcp:8080 tcp:8080   # re-run this any time adb reconnects (e.g. Android Studio re-attaching resets it)
```

See the root `TESTING.md` for exercising the create *and* restore ceremonies, not just installing the app.
