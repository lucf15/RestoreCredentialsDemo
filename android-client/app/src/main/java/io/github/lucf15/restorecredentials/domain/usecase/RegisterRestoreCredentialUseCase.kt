package io.github.lucf15.restorecredentials.domain.usecase

import io.github.lucf15.restorecredentials.domain.repository.RestoreCredentialApi
import io.github.lucf15.restorecredentials.domain.repository.RestoreCredentialGateway

class RegisterRestoreCredentialUseCase(
    private val restoreCredentialApi: RestoreCredentialApi,
    private val restoreCredentialGateway: RestoreCredentialGateway,
) {
    suspend operator fun invoke(accessToken: String) {
        val optionsJson = restoreCredentialApi.registrationOptions(accessToken)
        val registrationResponseJson = restoreCredentialGateway.register(optionsJson)
        restoreCredentialApi.verifyRegistration(accessToken, registrationResponseJson)
    }
}
