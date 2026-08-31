package io.github.lucf15.restorecredentials.server.domain.repository

import io.github.lucf15.restorecredentials.server.domain.model.RestoreCredentialRecord

interface RestoreCredentialRepository {
    fun save(record: RestoreCredentialRecord)

    fun findByUserId(userId: String): List<RestoreCredentialRecord>

    fun findByCredentialId(credentialId: ByteArray): RestoreCredentialRecord?

    fun updateSignatureCount(credentialId: ByteArray, newCount: Long)
}
