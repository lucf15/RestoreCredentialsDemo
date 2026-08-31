package io.github.lucf15.restorecredentials.server.data

import io.github.lucf15.restorecredentials.server.domain.model.RestoreCredentialRecord
import io.github.lucf15.restorecredentials.server.domain.repository.RestoreCredentialRepository
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

class InMemoryRestoreCredentialRepository : RestoreCredentialRepository {
    // Keyed by credential id so one user can hold several restore keys (one per device).
    private val byCredentialId = ConcurrentHashMap<String, RestoreCredentialRecord>()

    override fun save(record: RestoreCredentialRecord) {
        byCredentialId[key(record.credentialId)] = record
    }

    override fun findByUserId(userId: String): List<RestoreCredentialRecord> =
        byCredentialId.values.filter { it.userId == userId }

    override fun findByCredentialId(credentialId: ByteArray): RestoreCredentialRecord? = byCredentialId[key(credentialId)]

    override fun updateSignatureCount(credentialId: ByteArray, newCount: Long) {
        byCredentialId.computeIfPresent(key(credentialId)) { _, record -> record.withSignatureCount(newCount) }
    }

    private fun key(credentialId: ByteArray): String = Base64.getEncoder().encodeToString(credentialId)
}
