# Server

Kotlin + [Ktor](https://ktor.io/), acting as the WebAuthn relying party for Restore Credentials and issuing sessions. If you're new to WebAuthn: a "relying party" is just the server-side role that generates challenges, verifies signatures, and stores public keys — this module *is* that role for this demo.

## Endpoints

| Route | Auth | Purpose |
|---|---|---|
| `POST /auth/login` | — | Sign in with a username and password. Always succeeds: an unknown username is created on the spot, and the password is never actually checked (see below) — this demo's focus is restore credentials, not building a full auth system |
| `POST /auth/refresh` | — | Rotates the refresh token, issues a new access token |
| `GET /auth/me` | Bearer | Sanity-check endpoint |
| `POST /restore/register/options` | Bearer | WebAuthn creation options (`residentKey: required`) |
| `POST /restore/register/verify` | Bearer | Verifies the attestation, stores the credential |
| `POST /restore/authenticate/options` | — | WebAuthn request options (no username, `userVerification: discouraged`) |
| `POST /restore/authenticate/verify` | — | Verifies the assertion, issues a new session |

The `authenticate` routes are intentionally unauthenticated — there's no user to authenticate as until the assertion itself proves who they are.

## Running it

```bash
RP_ID=<your-domain> RP_ORIGIN=https://<your-domain> ANDROID_ORIGINS=android:apk-key-hash:<your-cert-hash> ./gradlew run
```

| Env var | Default | Purpose |
|---|---|---|
| `RP_ID` | `localhost` | The WebAuthn Relying Party ID — the domain the credential is bound to (see below). Startup logs a warning if left unset, since nothing will actually work against a real device. |
| `RP_ORIGIN` | `https://$RP_ID` | The origin a *browser* would send; kept separate from `RP_ID` because a native app sends something else entirely (see below). |
| `ANDROID_ORIGINS` | — | Comma-separated list of native-app origins to also accept, one per signing cert you test with (debug + release). |
| `RP_NAME` | `Restore Credentials Demo` | Display name only. |
| `JWT_SECRET` | random per boot | HMAC secret for access tokens. Set a stable value if you want tokens to survive a restart. |
| `PORT` | `8080` | — |

Get your debug keystore's certificate fingerprint with:
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA256
```
then base64url-encode the 32 raw bytes to get the value `ANDROID_ORIGINS` expects (see below for why it's shaped this way).

## Two pitfalls that cost real debugging time

### The origin is not what you'd guess

A native Android app's `clientDataJSON.origin` is **not** `https://<rpId>` — that's what a *browser* sends. Credential Manager sends `android:apk-key-hash:<base64url SHA-256 of the signing cert>` instead. `RelyingParty.origins` has to allow-list that string too (`ServerConfig.kt` merges `ANDROID_ORIGINS` into it). You don't have to guess the exact value — it shows up verbatim in the server's own error log the first time a real device tries to register:
```
Incorrect origin, please see the RelyingParty.origins setting: android:apk-key-hash:XXXXXXXX...
```

### Yubico's JSON isn't quite what Android wants

`RelyingParty.startRegistration(...).toCredentialsCreateJson()` (and `.toCredentialsGetJson()`) wrap the options in `{"publicKey": {...}}` — the shape a browser passes straight into `navigator.credentials.create()`. Android's `CreateRestoreCredentialRequest` / `GetRestoreCredentialOption` expect the **bare** object instead, with `user`/`rp`/`challenge` at the top level. `RestoreCredentialService.kt`'s `unwrapPublicKey()` strips the envelope before the JSON ever reaches the client — miss this and `CreateRestoreCredentialRequest`'s constructor throws `IllegalArgumentException: user.id must be defined in requestJson`, which reads like a completely unrelated bug the first time you hit it.

## Session model

Short-lived JWT access token (HMAC256, 15 minutes) + an opaque, rotating refresh token (30 days), issued together by `SessionService`. Refreshing deletes the old refresh token and issues a brand-new pair — reusing an old refresh token after that fails outright, which is what "rotating" buys you: a leaked-and-later-replayed token gets detected instead of silently working forever. A plain server-side session would be a perfectly legitimate alternative for a real deployment; this pairing was chosen mainly to have something worth writing about in the composition of the two pieces.

## What's a deliberate demo simplification

- **Everything is in-memory** (`data/InMemory*Repository.kt`) — restart the server and every user, credential, and token is gone. Swap those for real persistence before this touches production.
- **`/auth/login` never checks the password.** `AuthRoutes.kt` upserts by username alone: `users.findByUsername(...) ?: users.create(...)`. The password field exists in the request only so the client's sign-in screen looks like a real one — the point of this repo is the restore-credential layer underneath, not primary-auth security, and every user needs to be able to reach that layer to be worth demoing.
