package io.github.lucf15.restorecredentials.server.domain.model

import java.time.Instant

data class RefreshTokenRecord(val token: String, val userId: String, val expiresAt: Instant)
