package io.github.lucf15.restorecredentials.data.network

import io.github.lucf15.restorecredentials.domain.model.AuthSession
import io.github.lucf15.restorecredentials.domain.repository.AuthRepository
import io.github.lucf15.restorecredentials.domain.repository.TokenPair
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiClient(private val httpClient: HttpClient) : AuthRepository {
    override suspend fun login(username: String, password: String): AuthSession {
        val response: TokenPairResponseDto =
            httpClient
                .post("/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequestDto(username, password))
                }
                .body()
        return AuthSession(response.accessToken, response.refreshToken, response.username)
    }

    override suspend fun refresh(refreshToken: String): TokenPair {
        val response: RefreshResponseDto =
            httpClient
                .post("/auth/refresh") {
                    contentType(ContentType.Application.Json)
                    setBody(RefreshRequestDto(refreshToken))
                }
                .body()
        return TokenPair(response.accessToken, response.refreshToken)
    }

    override suspend fun logout(refreshToken: String) {
        httpClient.post("/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody(LogoutRequestDto(refreshToken))
        }
    }
}
