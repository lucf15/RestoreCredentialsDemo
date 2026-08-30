package io.github.lucf15.restorecredentials.server

import io.github.lucf15.restorecredentials.server.auth.JwtService
import io.github.lucf15.restorecredentials.server.auth.SessionService
import io.github.lucf15.restorecredentials.server.data.InMemoryRefreshTokenRepository
import io.github.lucf15.restorecredentials.server.data.InMemoryRestoreCredentialRepository
import io.github.lucf15.restorecredentials.server.data.InMemoryUserRepository
import io.github.lucf15.restorecredentials.server.plugins.configureErrorHandling
import io.github.lucf15.restorecredentials.server.plugins.configureLogging
import io.github.lucf15.restorecredentials.server.plugins.configureRouting
import io.github.lucf15.restorecredentials.server.plugins.configureSecurity
import io.github.lucf15.restorecredentials.server.plugins.configureSerialization
import io.github.lucf15.restorecredentials.server.webauthn.RestoreCredentialService
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Application")

fun main() {
    val config = ServerConfig.fromEnvironment()
    if (config.rpId == "localhost") {
        logger.warn(
            "RP_ID is not set - restore credentials need a real HTTPS domain with a hosted " +
                ".well-known/assetlinks.json. Set RP_ID and RP_ORIGIN before testing from the app."
        )
    }

    embeddedServer(Netty, port = config.port, module = { module(config) }).start(wait = true)
}

fun Application.module(config: ServerConfig) {
    val users = InMemoryUserRepository()
    val restoreCredentialRepository = InMemoryRestoreCredentialRepository()
    val refreshTokens = InMemoryRefreshTokenRepository()

    val jwtService = JwtService(secret = config.jwtSecret, issuer = config.jwtIssuer)
    val sessions = SessionService(jwtService, refreshTokens)
    val restoreCredentials =
        RestoreCredentialService(
            rpId = config.rpId,
            rpName = config.rpName,
            origins = config.origins,
            users = users,
            restoreCredentials = restoreCredentialRepository,
        )

    configureLogging()
    configureErrorHandling()
    configureSerialization()
    configureSecurity(jwtService)
    configureRouting(users, sessions, restoreCredentials)
}
