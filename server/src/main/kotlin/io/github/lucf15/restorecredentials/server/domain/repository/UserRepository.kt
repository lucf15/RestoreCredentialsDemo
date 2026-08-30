package io.github.lucf15.restorecredentials.server.domain.repository

import io.github.lucf15.restorecredentials.server.domain.model.User

interface UserRepository {
    fun create(username: String): User

    fun findByUsername(username: String): User?

    fun findById(id: String): User?

    fun findByUserHandle(userHandle: ByteArray): User?
}
