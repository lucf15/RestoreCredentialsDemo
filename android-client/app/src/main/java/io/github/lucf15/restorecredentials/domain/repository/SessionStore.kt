package io.github.lucf15.restorecredentials.domain.repository

import io.github.lucf15.restorecredentials.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface SessionStore {
    val session: Flow<AuthSession?>

    suspend fun save(session: AuthSession)

    suspend fun clear()
}
