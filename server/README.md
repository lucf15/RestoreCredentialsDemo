# Server

Kotlin + [Ktor](https://ktor.io/), acting as the WebAuthn relying party: it generates challenges, verifies signatures and attestations, stores public keys, and issues sessions.

## Endpoints

| Route | Auth | Purpose |
|---|---|---|
| `POST /auth/login` | none | Username + password. Always succeeds; unknown usernames are created, the password is not checked. |
| `POST /auth/refresh` | none | Rotates the refresh token, returns a new access token. |
| `POST /auth/logout` | none | Revokes the supplied refresh token. Idempotent; an unknown token still returns `204`. |
| `GET /auth/me` | Bearer | Returns the caller's user id and username. |
| `POST /restore/register/options` | Bearer | WebAuthn creation options (`residentKey: required`). |
| `POST /restore/register/verify` | Bearer | Verifies the attestation, stores the credential. |
| `POST /restore/authenticate/options` | none | WebAuthn request options (no username, `userVerification: discouraged`). |
| `POST /restore/authenticate/verify` | none | Verifies the assertion, issues a session. |

The `authenticate` routes take no auth because there is no user to authenticate as until the assertion proves who they are.

## Running it

```bash
RP_ID=<your-domain> RP_ORIGIN=https://<your-domain> ANDROID_ORIGINS=android:apk-key-hash:<your-cert-hash> ./gradlew run
```

| Env var | Default | Purpose |
|---|---|---|
| `RP_ID` | `localhost` | WebAuthn Relying Party ID: the domain the credential is bound to. Startup warns if unset. |
| `RP_ORIGIN` | `https://$RP_ID` | The origin a browser would send. Separate from `RP_ID` because a native app sends something else (below). |
| `ANDROID_ORIGINS` | none | Comma-separated native-app origins to also accept, one per signing cert (debug + release). |
| `RP_NAME` | `Restore Credentials Demo` | Display name. |
| `JWT_SECRET` | random per boot | HMAC secret for access tokens. Set a fixed value to keep tokens valid across restarts. |
| `PORT` | `8080` | |

Get the debug keystore's fingerprint:

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA256
```

then base64url-encode the 32 raw bytes for `ANDROID_ORIGINS`.

## Two pitfalls

### The origin string is not `https://<rpId>`

A native app's `clientDataJSON.origin` is `android:apk-key-hash:<base64url SHA-256 of the signing cert>`, not `https://<rpId>` (that is what a browser sends). `RelyingParty.origins` must allow-list it; `ServerConfig.kt` merges `ANDROID_ORIGINS` in. The exact value appears in the server log the first time a device registers:

```
Incorrect origin, please see the RelyingParty.origins setting: android:apk-key-hash:XXXXXXXX...
```

### Yubico's JSON has an extra envelope

`toCredentialsCreateJson()` / `toCredentialsGetJson()` wrap the options in `{"publicKey": {...}}`, the shape a browser passes to `navigator.credentials.create()`. `CreateRestoreCredentialRequest` / `GetRestoreCredentialOption` want the bare object, with `user` / `rp` / `challenge` at the top level. `unwrapPublicKey()` in `RestoreCredentialService.kt` strips the envelope. Without it the constructor throws `IllegalArgumentException: user.id must be defined in requestJson`.

## Session model

A 15-minute HMAC256 JWT access token plus an opaque 30-day refresh token, issued together by `SessionService`. Refreshing deletes the old refresh token and returns a new pair, so a replayed refresh token fails.

## Demo simplifications

- **Everything is in-memory** (`data/InMemory*Repository.kt`). A restart clears all users, credentials, and tokens.
- **`/auth/login` does not verify the password.** `AuthRoutes.kt` upserts by username: `users.findByUsername(...) ?: users.create(...)`. The password field only exists so the client's sign-in screen is realistic.
