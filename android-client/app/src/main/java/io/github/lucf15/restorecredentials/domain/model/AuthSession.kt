package io.github.lucf15.restorecredentials.domain.model

data class AuthSession(val accessToken: String, val refreshToken: String, val username: String)
