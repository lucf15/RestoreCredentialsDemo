package io.github.lucf15.restorecredentials.server.plugins

import io.github.lucf15.restorecredentials.server.auth.JwtService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureSecurity(jwtService: JwtService) {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(jwtService.verifier)
            validate { credential -> credential.payload.subject?.let { JWTPrincipal(credential.payload) } }
        }
    }
}
