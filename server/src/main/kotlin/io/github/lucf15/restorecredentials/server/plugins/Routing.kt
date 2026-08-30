package io.github.lucf15.restorecredentials.server.plugins

import io.github.lucf15.restorecredentials.server.auth.SessionService
import io.github.lucf15.restorecredentials.server.domain.repository.UserRepository
import io.github.lucf15.restorecredentials.server.routes.authRoutes
import io.github.lucf15.restorecredentials.server.routes.restoreCredentialRoutes
import io.github.lucf15.restorecredentials.server.webauthn.RestoreCredentialService
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting(users: UserRepository, sessions: SessionService, restoreCredentials: RestoreCredentialService) {
    routing {
        authRoutes(users, sessions)
        restoreCredentialRoutes(users, sessions, restoreCredentials)
    }
}
