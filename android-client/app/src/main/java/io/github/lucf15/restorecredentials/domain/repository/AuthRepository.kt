package io.github.lucf15.restorecredentials.domain.repository

import io.github.lucf15.restorecredentials.domain.model.AuthSession

data class TokenPair(val accessToken: String, val refreshToken: String)

interface AuthRepository {
    suspend fun login(username: String, password: String): AuthSession

    suspend fun refresh(refreshToken: String): TokenPair

    suspend fun logout(refreshToken: String)
}
