package io.github.lucf15.restorecredentials.server.routes

import io.github.lucf15.restorecredentials.server.auth.SessionService
import io.github.lucf15.restorecredentials.server.domain.repository.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.authRoutes(users: UserRepository, sessions: SessionService) {
    post("/auth/login") {
        val request = call.receive<LoginRequest>()
        val user = users.findByUsername(request.username) ?: users.create(request.username)
        val tokens = sessions.issueTokens(user.id)
        call.respond(TokenPairResponse(tokens.accessToken, tokens.refreshToken, user.username))
    }

    post("/auth/refresh") {
        val request = call.receive<RefreshRequest>()
        val tokens = sessions.refresh(request.refreshToken)
        if (tokens == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid or expired refresh token")
            return@post
        }
        call.respond(RefreshResponse(tokens.accessToken, tokens.refreshToken))
    }

    post("/auth/logout") {
        sessions.logout(call.receive<LogoutRequest>().refreshToken)
        call.respond(HttpStatusCode.NoContent)
    }

    authenticate("auth-jwt") {
        get("/auth/me") {
            val userId = call.principal<JWTPrincipal>()!!.payload.subject
            val user = users.findById(userId)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            call.respond(MeResponse(user.id, user.username))
        }
    }
}
