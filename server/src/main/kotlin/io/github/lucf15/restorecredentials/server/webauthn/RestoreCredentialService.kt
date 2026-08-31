package io.github.lucf15.restorecredentials.server.webauthn

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.yubico.webauthn.AssertionRequest
import com.yubico.webauthn.FinishAssertionOptions
import com.yubico.webauthn.FinishRegistrationOptions
import com.yubico.webauthn.RelyingParty
import com.yubico.webauthn.StartAssertionOptions
import com.yubico.webauthn.StartRegistrationOptions
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria
import com.yubico.webauthn.data.PublicKeyCredential
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions
import com.yubico.webauthn.data.RelyingPartyIdentity
import com.yubico.webauthn.data.ResidentKeyRequirement
import com.yubico.webauthn.data.UserIdentity
import com.yubico.webauthn.data.UserVerificationRequirement
import io.github.lucf15.restorecredentials.server.domain.model.RestoreCredentialRecord
import io.github.lucf15.restorecredentials.server.domain.model.User
import io.github.lucf15.restorecredentials.server.domain.repository.RestoreCredentialRepository
import io.github.lucf15.restorecredentials.server.domain.repository.UserRepository
import java.time.Duration
import java.util.UUID
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import com.yubico.webauthn.data.ByteArray as YubicoByteArray

private fun unwrapPublicKey(credentialsJson: String): String = Json.parseToJsonElement(credentialsJson).jsonObject.getValue("publicKey").toString()

class RestoreCredentialService(
    rpId: String,
    rpName: String,
    origins: Set<String>,
    private val users: UserRepository,
    private val restoreCredentials: RestoreCredentialRepository,
) {
    private val relyingParty: RelyingParty =
        RelyingParty.builder()
            .identity(RelyingPartyIdentity.builder().id(rpId).name(rpName).build())
            .credentialRepository(YubicoCredentialRepositoryAdapter(restoreCredentials, users))
            .origins(origins)
            // Restore credentials always report a signature count of 0; don't validate it.
            .validateSignatureCounter(false)
            .build()

    // In-flight ceremony state; entries expire so abandoned flows (and floods on the unauthenticated
    // /restore/authenticate/options) can't grow unbounded.
    private val pendingRegistrations: Cache<String, PublicKeyCredentialCreationOptions> =
        Caffeine.newBuilder().maximumSize(MAX_PENDING).expireAfterWrite(PENDING_TTL).build()
    private val pendingAssertions: Cache<String, AssertionRequest> =
        Caffeine.newBuilder().maximumSize(MAX_PENDING).expireAfterWrite(PENDING_TTL).build()

    fun startRegistration(user: User): String {
        val options =
            relyingParty.startRegistration(
                StartRegistrationOptions.builder()
                    .user(
                        UserIdentity.builder()
                            .name(user.username)
                            .displayName(user.username)
                            .id(user.userHandle.toYubico())
                            .build()
                    )
                    .authenticatorSelection(
                        AuthenticatorSelectionCriteria.builder()
                            .residentKey(ResidentKeyRequirement.REQUIRED)
                            .userVerification(UserVerificationRequirement.PREFERRED)
                            .build()
                    )
                    .build()
            )
        pendingRegistrations.put(user.id, options)
        return unwrapPublicKey(options.toCredentialsCreateJson())
    }

    fun finishRegistration(user: User, registrationResponseJson: String) {
        val options =
            pendingRegistrations.asMap().remove(user.id)
                ?: error("No pending restore-credential registration for ${user.username}")
        val pkc = PublicKeyCredential.parseRegistrationResponseJson(registrationResponseJson)
        val result = relyingParty.finishRegistration(FinishRegistrationOptions.builder().request(options).response(pkc).build())

        restoreCredentials.save(
            RestoreCredentialRecord(
                credentialId = result.keyId.id.bytes,
                userId = user.id,
                userHandle = user.userHandle,
                publicKeyCose = result.publicKeyCose.bytes,
                signatureCount = result.signatureCount,
            )
        )
    }

    fun startAuthentication(): Pair<String, String> {
        val request = relyingParty.startAssertion(StartAssertionOptions.builder().userVerification(UserVerificationRequirement.DISCOURAGED).build())
        val requestId = UUID.randomUUID().toString()
        pendingAssertions.put(requestId, request)
        return requestId to unwrapPublicKey(request.toCredentialsGetJson())
    }

    fun finishAuthentication(requestId: String, authenticationResponseJson: String): User {
        val request =
            pendingAssertions.asMap().remove(requestId) ?: error("No pending restore-credential assertion for $requestId")
        val pkc = PublicKeyCredential.parseAssertionResponseJson(authenticationResponseJson)
        val result = relyingParty.finishAssertion(FinishAssertionOptions.builder().request(request).response(pkc).build())

        restoreCredentials.updateSignatureCount(result.credential.credentialId.bytes, result.signatureCount)
        return users.findByUsername(result.username) ?: error("No user found for verified assertion")
    }

    private companion object {
        const val MAX_PENDING = 10_000L
        val PENDING_TTL: Duration = Duration.ofMinutes(5)
    }
}
