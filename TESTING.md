# Testing

`$DEV` is a device serial from `adb devices -l`; `$PKG` is `io.github.lucf15.restorecredentials`. Drop `-s $DEV` if only one device is attached.

## 1. Start the server

```bash
cd server
RP_ID=<your-domain> RP_ORIGIN=https://<your-domain> ANDROID_ORIGINS=android:apk-key-hash:<your-debug-cert-hash> ./gradlew run
```

See `server/README.md` for the variables and the cert hash. Leave it running and watch its log.

## 2. Build and install the app

```bash
cd android-client
./gradlew :app:assembleDebug
adb -s $DEV install -r -t app/build/outputs/apk/debug/app-debug.apk
adb -s $DEV reverse tcp:8080 tcp:8080
```

## 3. Check the server directly

```bash
curl -s -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"username":"test","password":"anything"}'
```

Returns an access token and a refresh token. Sign-in always succeeds and creates the account on first use.

## 4. Create ceremony

Sign in in the app, then check the server log for, in order:
`POST /auth/login` (200) → `POST /restore/register/options` (200) → `POST /restore/register/verify` (204).

The 204 means a resident key was created and verified server-side.

## 5. Restore ceremony

"Just reinstall the app" does not exercise this. Android has three backup transports and only one carries a restore credential:

- **Local transport** (`adb shell bmgr backupnow` / `restore`) carries only what your app declares for backup. Restore credentials live in Google Play Services' own store, not in that stream. If a local restore lands on the signed-in screen, that is a restored session file, not a restored credential, which is why this app excludes its session file from backup (`res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml`). A correct local restore lands on the sign-in screen.
- **D2D transport** via `bmgr restore` refuses: `[D2dTransport] Can't restore from D2d Transport.` It only runs through the real Setup Wizard migration.
- **Cloud transport** carries the credential, but needs a signed-in Google account, "Backup by Google One" turned on, and a screen lock for end-to-end encryption. The first backup after opting in fails with "Encryption key has not synced" until Google's servers catch up.

### Android Studio's Backup/Restore App Data

The [documented way](https://developer.android.com/identity/sign-in/test-restore-credentials) to test this. Needs Android Studio Otter (2025.2.1) or newer and a debuggable build. Google's docs note this flow "simulates the setup wizard flow", the same privileged path a real device-to-device migration uses.

In the Running Devices toolbar: **Backup App Data** and **Restore App Data** (`Run` menu or toolbar icon). Pick **Device to Device**, back up on the signed-in device, then restore on another device from the resulting `.backup` file. With `android:allowBackup="false"` only Device to Device restores work this way; it is `true` here, so Cloud restores work too.

Watch the server log for `POST /restore/authenticate/verify` (200): the credential round-tripped.

## Watching both sides

```bash
# Server
tail -f /tmp/server.log | grep --line-buffered "restore\|login"

# Client
adb -s $DEV logcat -c && adb -s $DEV logcat | grep "RestoreCredentialBackupAgent"
```

If `onRestoreFinished: fired` appears in logcat but nothing reaches the server, the app crashed before the call; check logcat for a stack trace.

## Other notes

- **`adb reverse` does not survive Android Studio re-attaching a device.** Re-run it, or requests time out.
- **`android:fullBackupOnly="true"` is required** on `<application>` once you declare a custom `android:backupAgent`, or Android Studio's backup tooling fails with "App did not provide any backup data".
