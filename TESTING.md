# Testing this yourself

Everywhere below, `$DEV` is a device serial from `adb devices -l` and `$PKG` is `io.github.lucf15.restorecredentials`. If you only have one device/emulator attached, drop `-s $DEV` entirely.

## 1. Start the server

```bash
cd server
RP_ID=<your-domain> RP_ORIGIN=https://<your-domain> ANDROID_ORIGINS=android:apk-key-hash:<your-debug-cert-hash> ./gradlew run
```

See `server/README.md` for what each variable means and how to get the cert hash. Leave this running — you'll want to watch its log output through every step below.

## 2. Build and install the app

```bash
cd android-client
./gradlew :app:assembleDebug
adb -s $DEV install -r -t app/build/outputs/apk/debug/app-debug.apk
adb -s $DEV reverse tcp:8080 tcp:8080
```

## 3. Sanity-check the server directly

```bash
curl -s -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"username":"test","password":"anything"}'
```

You should get back a JSON body with an access token and refresh token. Sign-in always succeeds and creates the account on first use — see `server/README.md` for why the password isn't actually checked.

## 4. Test the create ceremony

Sign in in the app UI, then check the server log for, in order:
`POST /auth/login` (200) → `POST /restore/register/options` (200) → `POST /restore/register/verify` (**204**).

A 204 there means a real WebAuthn resident key was created and verified server-side — not just that the sign-in screen closed.

## 5. Test the restore ceremony

This is the part that actually needs explaining, because "just reinstall the app" doesn't exercise the real code path.

### Why the obvious things don't work

Android has three separate backup transports, and only one of them actually carries the data a restore credential lives in:

- **Local transport** (`adb shell bmgr backupnow`/`restore` with no transport selected) only carries whatever your app explicitly declares for backup — files, `BackupAgent` key-value data. It does **not** carry restore credentials; they live in Google Play Services' own credential store, entirely separate from your app's declared backup content. If a local-transport restore lands you on the signed-in screen, you're seeing a locally-restored session file, not a real restored credential — which is exactly why this app excludes its session file from backup (`res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml`). A correct local-transport restore should land you back on the sign-in screen.
- **D2D (device-to-device) transport**, driven directly via `bmgr restore`, refuses outright: `[D2dTransport] Can't restore from D2d Transport.` — a deliberate guard in Play Services' own code. It only works through the real Setup Wizard device-to-device migration flow, which isn't independently scriptable.
- **Cloud transport** is the one that actually carries the credential — but only with a real signed-in Google account, "Backup by Google One" explicitly turned on (Settings → Backup), and a screen lock set for the end-to-end encryption. Even then, the first backup after opting in fails with "Encryption key has not synced" while Google's servers catch up — a real async delay, sometimes several minutes.

That last path is real but slow and account-dependent, which makes it painful to iterate on. There's a much faster local option:

### Android Studio's Backup/Restore App Data

This is the [officially documented way](https://developer.android.com/identity/sign-in/test-restore-credentials) to test this. It needs **Android Studio Otter (2025.2.1) or newer**, and the app built `debuggable` (the default for a debug build). Per Google's own docs, this isn't just a shortcut around real backup transports — Studio's restore flow **"simulates the setup wizard flow"**, the same privileged path a real device-to-device migration goes through during Android's actual setup, which is more than any sequence of `bmgr`/`adb` commands run from a terminal can trigger on their own.

The Running Devices toolbar has **Backup App Data** and **Restore App Data** actions (`Run` menu, or the toolbar icon). Pick **Device to Device** as the backup type, run a backup on the device that's signed in, then run a restore on a different (or freshly wiped) device using the resulting `.backup` file. If `android:allowBackup` is `false` in your manifest (it's `true` in this one), Google's docs note that only Device to Device restores work this way — Cloud restores need it `true`.

Watch the server log for `POST /restore/authenticate/verify` (200) — that's confirmation the credential round-tripped for real, independent of whatever the app UI happens to show.

## Watching both sides live

```bash
# Server: the restore-credential lifecycle
tail -f /tmp/server.log | grep --line-buffered "restore\|login"

# Client: BackupAgent's background restore attempt
adb -s $DEV logcat -c && adb -s $DEV logcat | grep "RestoreCredentialBackupAgent"
```

If the background hook fires (`onRestoreFinished: fired` in logcat) but nothing reaches the server, the app crashed before it could make the call — check logcat for a stack trace rather than assuming the credential itself is missing.

## Everything else that bit us, briefly

- **`adb reverse` doesn't survive Android Studio re-attaching to a device.** Re-run it after Studio grabs the device, or requests just time out with no useful error.
- **`android:fullBackupOnly="true"` is required** on the `<application>` tag once you declare a custom `android:backupAgent` — otherwise Android Studio's backup tooling fails with "App did not provide any backup data," because it tries the (empty, by design) key-value `onBackup()` path instead of full-data backup.
