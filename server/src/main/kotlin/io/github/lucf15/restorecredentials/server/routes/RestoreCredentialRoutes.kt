package io.github.lucf15.restorecredentials.server.routes

import io.github.lucf15.restorecredentials.server.auth.SessionService
import io.github.lucf15.restorecredentials.server.domain.repository.UserRepository
import io.github.lucf15.restorecredentials.server.webauthn.RestoreCredentialService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.restoreCredentialRoutes(users: UserRepository, sessions: SessionService, restoreCredentials: RestoreCredentialService) {
    authenticate("auth-jwt") {
        post("/restore/register/options") {
            val userId = call.principal<JWTPrincipal>()!!.payload.subject
            val user = users.findById(userId)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound)
                return@post
            }
            call.respond(RestoreRegistrationOptionsResponse(restoreCredentials.startRegistration(user)))
        }

        post("/restore/register/verify") {
            val userId = call.principal<JWTPrincipal>()!!.payload.subject
            val user = users.findById(userId)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound)
                return@post
            }
            val request = call.receive<RestoreRegistrationVerifyRequest>()
            runCatching { restoreCredentials.finishRegistration(user, request.registrationResponseJson) }
                .onSuccess { call.respond(HttpStatusCode.NoContent) }
                .onFailure {
                    call.application.log.error("Restore-credential registration failed for ${user.username}", it)
                    call.respond(HttpStatusCode.BadRequest, it.message ?: "Registration failed")
                }
        }
    }

    post("/restore/authenticate/options") {
        val (requestId, requestJson) = restoreCredentials.startAuthentication()
        call.respond(RestoreAuthenticationOptionsResponse(requestId, requestJson))
    }

    post("/restore/authenticate/verify") {
        val request = call.receive<RestoreAuthenticationVerifyRequest>()
        runCatching { restoreCredentials.finishAuthentication(request.requestId, request.authenticationResponseJson) }
            .onSuccess { user ->
                val tokens = sessions.issueTokens(user.id)
                call.respond(TokenPairResponse(tokens.accessToken, tokens.refreshToken, user.username))
            }
            .onFailure {
                call.application.log.error("Restore-credential sign-in failed", it)
                call.respond(HttpStatusCode.Unauthorized, it.message ?: "Restore sign-in failed")
            }
    }
}
