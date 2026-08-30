# Restore Credentials Demo

A working, end-to-end sample of Android's [Restore Credentials](https://developer.android.com/identity/sign-in/restore-credentials) API: a Kotlin/Compose Android client and a Kotlin/Ktor backend, demonstrating zero-tap sign-in after a user reinstalls your app on a new device.

Restore Credentials will be required by Google Play for apps with sign-in, starting April 2027 ([announcement](https://android-developers.googleblog.com/2026/08/app-quality-memory-optimization-secure-onboarding.html)). This repo is a minimal, real implementation of both sides of that contract — not a mockup. Every claim in these docs was checked against a real device: signing up creates a real credential server-side, and restoring on a second device really lands the user on the signed-in screen with zero taps.

<p align="center">
  <img src="docs/screenshots/signin.png" alt="Sign-in screen" width="280" />
  &nbsp;&nbsp;
  <img src="docs/screenshots/home.png" alt="Home screen, signed in" width="280" />
</p>

## Repo layout

```
server/            Ktor backend - WebAuthn relying party + session issuance. See server/README.md
android-client/    Android app - Compose UI, Credential Manager integration. See android-client/README.md
TESTING.md         How to exercise the whole flow yourself, including the restore ceremony
```

## The mental model

Restore Credentials is **WebAuthn/FIDO2 under the hood** — the same protocol passkeys use. If you've never touched WebAuthn: it's a challenge/response scheme where a device holds a private key and a server holds the matching public key, and "signing in" means proving possession of the private key without ever sending it anywhere. `CreateRestoreCredentialRequest` and `RestoreCredential` (the Android APIs this repo uses) carry standard `PublicKeyCredentialCreationOptionsJSON` / `AuthenticationResponseJSON` payloads — the exact same JSON shapes a browser would exchange for a passkey. The only restore-specific choices are:

- Registration requires a **resident (discoverable) key**, so the credential can be found on a brand-new device before the app knows who the user is.
- Authentication is **usernameless** — there's no signed-in user yet on a fresh install, so the client never sends one; the device just presents whatever resident key it's holding for this app.
- The credential is stored **separately from ordinary passkeys** on-device, hidden from any passkey-management UI, one per app per device.

### The flow

1. User signs in (or is already signed in) → server issues a session (a short-lived JWT access token plus an opaque, rotating refresh token, in this demo).
2. The app silently asks the server for WebAuthn registration options, calls `CredentialManager.createCredential()` with a `CreateRestoreCredentialRequest`, and sends the result back to the server for verification. No UI, no user interaction — this happens right after sign-in.
3. Later, on a new device (or after the app is reinstalled from a backup), the app calls `CredentialManager.getCredential()` with a `GetRestoreCredentialOption` — no username. If a restore credential exists, it comes back with zero taps; the app sends it to the server, gets a fresh session, and the user is signed in before they've touched anything.
4. If nothing comes back (a `NoCredentialException` under the hood), the app falls through to a normal sign-in screen.

Google's [implementation guide](https://developer.android.com/identity/sign-in/restore-credentials-implementation) recommends fetching the restore credential in two scenarios, and this repo implements both:

- **A background hook**, `BackupAgent.onRestoreFinished()` — called right after your app's data has been restored, before the user has even opened the app. This is what makes the "zero taps" claim literal: notifications and sync can resume before the icon is ever tapped.
- **A plain foreground check on first launch**, covering the case where the background hook doesn't fire — dropped network, `allowBackup=false`, or (as this repo found out the hard way — see `android-client/README.md`) a process-lifecycle quirk in how `BackupAgent` gets spun up.

## A domain you control is not optional

Android's Credential Manager binds every restore credential to a real HTTPS domain via [Digital Asset Links](https://developers.google.com/digital-asset-links/v1/getting-started) — there's no `localhost` exemption like browser WebAuthn has. Before any of this works you need:

1. A domain you can publish a static file to.
2. `.well-known/assetlinks.json` at that domain, declaring your app's package name and signing-cert SHA-256 fingerprint. See `assetlinks.json` in this repo for the exact shape, and `server/README.md` for how to derive the fingerprint.
3. `RP_ID` / `RP_ORIGIN` environment variables pointed at that domain when starting the server (see `server/README.md`).

Your actual API traffic does **not** need to be public — only the static `assetlinks.json` file does. The app can keep talking to a server on `localhost` via `adb reverse tcp:8080 tcp:8080` (works over USB on a physical device too); Android fetches `assetlinks.json` straight from Google's own infrastructure, independent of where your API lives.

## Quick start

```bash
# 1. Server (see server/README.md for the env vars and why they're required)
cd server
RP_ID=<your-domain> RP_ORIGIN=https://<your-domain> ANDROID_ORIGINS=android:apk-key-hash:<your-cert-hash> ./gradlew run

# 2. App (see android-client/README.md for the architecture)
cd android-client
./gradlew :app:assembleDebug
adb install -r -t app/build/outputs/apk/debug/app-debug.apk
adb reverse tcp:8080 tcp:8080
```

See `TESTING.md` for the full walkthrough, including how to actually exercise the restore ceremony (not just the sign-in half).

## What's a deliberate demo simplification

- **In-memory storage everywhere** (server users/credentials/tokens, nothing persisted to disk). Swap for a real database before shipping anything.
- **The sign-in screen takes a password, but the server never checks it** — any username/password signs you in, creating the account on first use. The point of this repo is the restore-credential layer, which is designed to sit *underneath* whatever primary auth you already have; plugging in real password verification wouldn't change anything about the restore-credential code above it.
- **JWT access token + opaque rotating refresh token**, chosen over a plain server-side session for demo clarity of how the two compose. Either is a legitimate choice for a real deployment; see `server/README.md` for the tradeoffs.
