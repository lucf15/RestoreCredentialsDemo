package io.github.lucf15.restorecredentials.server.data

import io.github.lucf15.restorecredentials.server.domain.model.RestoreCredentialRecord
import io.github.lucf15.restorecredentials.server.domain.repository.RestoreCredentialRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryRestoreCredentialRepository : RestoreCredentialRepository {
    private val byUserId = ConcurrentHashMap<String, RestoreCredentialRecord>()

    override fun save(record: RestoreCredentialRecord) {
        byUserId[record.userId] = record
    }

    override fun findByUserId(userId: String): RestoreCredentialRecord? = byUserId[userId]

    override fun findByCredentialId(credentialId: ByteArray): RestoreCredentialRecord? =
        byUserId.values.find { it.credentialId.contentEquals(credentialId) }

    override fun updateSignatureCount(credentialId: ByteArray, newCount: Long) {
        val record = findByCredentialId(credentialId) ?: return
        byUserId[record.userId] = record.withSignatureCount(newCount)
    }
}
