package io.github.lucf15.restorecredentials.domain.repository

sealed interface RestoreSignInOutcome {
    data class Available(val authenticationResponseJson: String) : RestoreSignInOutcome

    data object NotAvailable : RestoreSignInOutcome
}

interface RestoreCredentialGateway {
    suspend fun register(creationOptionsJson: String): String

    suspend fun signIn(requestOptionsJson: String): RestoreSignInOutcome

    suspend fun clear()
}
