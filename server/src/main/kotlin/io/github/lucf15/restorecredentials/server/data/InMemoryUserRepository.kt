package io.github.lucf15.restorecredentials.server.data

import io.github.lucf15.restorecredentials.server.domain.model.User
import io.github.lucf15.restorecredentials.server.domain.repository.UserRepository
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryUserRepository : UserRepository {
    private val byId = ConcurrentHashMap<String, User>()
    private val idByUsername = ConcurrentHashMap<String, String>()
    private val random = SecureRandom()

    override fun create(username: String): User {
        val userHandle = ByteArray(32).also(random::nextBytes)
        val user = User(id = UUID.randomUUID().toString(), username = username, userHandle = userHandle)
        byId[user.id] = user
        idByUsername[username] = user.id
        return user
    }

    override fun findByUsername(username: String): User? = idByUsername[username]?.let(byId::get)

    override fun findById(id: String): User? = byId[id]

    override fun findByUserHandle(userHandle: ByteArray): User? = byId.values.find { it.userHandle.contentEquals(userHandle) }
}
