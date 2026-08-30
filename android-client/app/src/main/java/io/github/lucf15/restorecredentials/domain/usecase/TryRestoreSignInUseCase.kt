package io.github.lucf15.restorecredentials.domain.usecase

import io.github.lucf15.restorecredentials.domain.repository.RestoreCredentialApi
import io.github.lucf15.restorecredentials.domain.repository.RestoreCredentialGateway
import io.github.lucf15.restorecredentials.domain.repository.RestoreSignInOutcome
import io.github.lucf15.restorecredentials.domain.repository.SessionStore

class TryRestoreSignInUseCase(
    private val restoreCredentialApi: RestoreCredentialApi,
    private val restoreCredentialGateway: RestoreCredentialGateway,
    private val sessionStore: SessionStore,
) {
    suspend operator fun invoke(): Boolean {
        val (requestId, requestJson) = restoreCredentialApi.authenticationOptions()
        val outcome = restoreCredentialGateway.signIn(requestJson)
        val authenticationResponseJson =
            when (outcome) {
                is RestoreSignInOutcome.Available -> outcome.authenticationResponseJson
                RestoreSignInOutcome.NotAvailable -> return false
            }
        val session = restoreCredentialApi.verifyAuthentication(requestId, authenticationResponseJson)
        sessionStore.save(session)
        return true
    }
}
