package io.github.lucf15.restorecredentials.data.network

import io.github.lucf15.restorecredentials.domain.model.AuthSession
import io.github.lucf15.restorecredentials.domain.repository.RestoreCredentialApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RestoreCredentialApiClient(private val httpClient: HttpClient) : RestoreCredentialApi {
    override suspend fun registrationOptions(accessToken: String): String {
        val response: RestoreRegistrationOptionsResponseDto = httpClient.post("/restore/register/options") { bearerAuth(accessToken) }.body()
        return response.requestJson
    }

    override suspend fun verifyRegistration(accessToken: String, registrationResponseJson: String) {
        httpClient.post("/restore/register/verify") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(RestoreRegistrationVerifyRequestDto(registrationResponseJson))
        }
    }

    override suspend fun authenticationOptions(): Pair<String, String> {
        val response: RestoreAuthenticationOptionsResponseDto = httpClient.post("/restore/authenticate/options").body()
        return response.requestId to response.requestJson
    }

    override suspend fun verifyAuthentication(requestId: String, authenticationResponseJson: String): AuthSession {
        val response: TokenPairResponseDto =
            httpClient
                .post("/restore/authenticate/verify") {
                    contentType(ContentType.Application.Json)
                    setBody(RestoreAuthenticationVerifyRequestDto(requestId, authenticationResponseJson))
                }
                .body()
        return AuthSession(response.accessToken, response.refreshToken, response.username)
    }
}
