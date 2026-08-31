package io.github.lucf15.restorecredentials.server.auth

import io.github.lucf15.restorecredentials.server.domain.model.RefreshTokenRecord
import io.github.lucf15.restorecredentials.server.domain.repository.RefreshTokenRepository
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64

class SessionService(
    private val jwtService: JwtService,
    private val refreshTokens: RefreshTokenRepository,
    private val refreshTokenTtl: Duration = Duration.ofDays(30),
) {
    private val random = SecureRandom()

    data class TokenPair(val accessToken: String, val refreshToken: String)

    fun issueTokens(userId: String): TokenPair {
        val refreshToken = newOpaqueToken()
        refreshTokens.save(RefreshTokenRecord(refreshToken, userId, Instant.now().plus(refreshTokenTtl)))
        return TokenPair(jwtService.issueAccessToken(userId), refreshToken)
    }

    fun refresh(refreshToken: String): TokenPair? {
        val record = refreshTokens.findAndDelete(refreshToken) ?: return null
        if (record.expiresAt.isBefore(Instant.now())) return null
        return issueTokens(record.userId)
    }

    /** Revokes the supplied refresh token (this device's session). Idempotent. */
    fun logout(refreshToken: String) {
        refreshTokens.delete(refreshToken)
    }

    private fun newOpaqueToken(): String {
        val bytes = ByteArray(32).also(random::nextBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
