package io.github.lucf15.restorecredentials.server.domain.repository

import io.github.lucf15.restorecredentials.server.domain.model.RefreshTokenRecord

interface RefreshTokenRepository {
    fun save(record: RefreshTokenRecord)

    fun find(token: String): RefreshTokenRecord?

    fun delete(token: String)

    fun deleteAllForUser(userId: String)
}
