package io.github.lucf15.restorecredentials.server.webauthn

import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.RegisteredCredential
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor
import io.github.lucf15.restorecredentials.server.domain.model.RestoreCredentialRecord
import io.github.lucf15.restorecredentials.server.domain.repository.RestoreCredentialRepository
import io.github.lucf15.restorecredentials.server.domain.repository.UserRepository
import java.util.Optional
import com.yubico.webauthn.data.ByteArray as YubicoByteArray

class YubicoCredentialRepositoryAdapter(
    private val restoreCredentials: RestoreCredentialRepository,
    private val users: UserRepository,
) : CredentialRepository {

    override fun getCredentialIdsForUsername(username: String): MutableSet<PublicKeyCredentialDescriptor> {
        val user = users.findByUsername(username) ?: return mutableSetOf()
        return restoreCredentials.findByUserId(user.id)
            .mapTo(mutableSetOf()) { PublicKeyCredentialDescriptor.builder().id(it.credentialId.toYubico()).build() }
    }

    override fun getUserHandleForUsername(username: String): Optional<YubicoByteArray> =
        Optional.ofNullable(users.findByUsername(username)?.userHandle?.toYubico())

    override fun getUsernameForUserHandle(userHandle: YubicoByteArray): Optional<String> =
        Optional.ofNullable(users.findByUserHandle(userHandle.bytes)?.username)

    override fun lookup(credentialId: YubicoByteArray, userHandle: YubicoByteArray): Optional<RegisteredCredential> {
        val record = restoreCredentials.findByCredentialId(credentialId.bytes) ?: return Optional.empty()
        if (!record.userHandle.contentEquals(userHandle.bytes)) return Optional.empty()
        return Optional.of(record.toRegisteredCredential())
    }

    override fun lookupAll(credentialId: YubicoByteArray): MutableSet<RegisteredCredential> {
        val record = restoreCredentials.findByCredentialId(credentialId.bytes) ?: return mutableSetOf()
        return mutableSetOf(record.toRegisteredCredential())
    }
}

internal fun ByteArray.toYubico(): YubicoByteArray = YubicoByteArray(this)

internal fun RestoreCredentialRecord.toRegisteredCredential(): RegisteredCredential =
    RegisteredCredential.builder()
        .credentialId(credentialId.toYubico())
        .userHandle(userHandle.toYubico())
        .publicKeyCose(publicKeyCose.toYubico())
        .signatureCount(signatureCount)
        .build()
