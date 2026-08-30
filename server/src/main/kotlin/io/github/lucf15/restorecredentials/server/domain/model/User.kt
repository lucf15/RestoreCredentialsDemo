package io.github.lucf15.restorecredentials.server.domain.model

import java.time.Instant

class User(val id: String, val username: String, val userHandle: ByteArray, val createdAt: Instant = Instant.now()) {
    override fun equals(other: Any?): Boolean = other is User && id == other.id

    override fun hashCode(): Int = id.hashCode()
}
