package io.github.lucf15.restorecredentials.domain.repository

import io.github.lucf15.restorecredentials.domain.model.AuthSession

interface RestoreCredentialApi {
    suspend fun registrationOptions(accessToken: String): String

    suspend fun verifyRegistration(accessToken: String, registrationResponseJson: String)

    suspend fun authenticationOptions(): Pair<String, String>

    suspend fun verifyAuthentication(requestId: String, authenticationResponseJson: String): AuthSession
}
