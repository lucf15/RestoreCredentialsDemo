package io.github.lucf15.restorecredentials.server.domain.model

class RestoreCredentialRecord(
    val credentialId: ByteArray,
    val userId: String,
    val userHandle: ByteArray,
    val publicKeyCose: ByteArray,
    val signatureCount: Long,
) {
    fun withSignatureCount(newCount: Long) = RestoreCredentialRecord(credentialId, userId, userHandle, publicKeyCose, newCount)
}
