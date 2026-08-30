package io.github.lucf15.restorecredentials.server.data

import io.github.lucf15.restorecredentials.server.domain.model.RefreshTokenRecord
import io.github.lucf15.restorecredentials.server.domain.repository.RefreshTokenRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryRefreshTokenRepository : RefreshTokenRepository {
    private val byToken = ConcurrentHashMap<String, RefreshTokenRecord>()

    override fun save(record: RefreshTokenRecord) {
        byToken[record.token] = record
    }

    override fun find(token: String): RefreshTokenRecord? = byToken[token]

    override fun delete(token: String) {
        byToken.remove(token)
    }

    override fun deleteAllForUser(userId: String) {
        byToken.values.filter { it.userId == userId }.forEach { byToken.remove(it.token) }
    }
}
