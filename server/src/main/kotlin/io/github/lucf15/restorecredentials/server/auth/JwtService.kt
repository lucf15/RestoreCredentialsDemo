package io.github.lucf15.restorecredentials.server.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.time.Duration
import java.time.Instant
import java.util.Date

class JwtService(secret: String, private val issuer: String, private val accessTokenTtl: Duration = Duration.ofMinutes(15)) {
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm).withIssuer(issuer).build()

    fun issueAccessToken(userId: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withSubject(userId)
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(Date.from(Instant.now().plus(accessTokenTtl)))
            .sign(algorithm)
}
