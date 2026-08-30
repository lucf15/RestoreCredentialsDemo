package io.github.lucf15.restorecredentials.domain.usecase

import io.github.lucf15.restorecredentials.domain.repository.RestoreCredentialGateway
import io.github.lucf15.restorecredentials.domain.repository.SessionStore

class SignOutUseCase(private val sessionStore: SessionStore, private val restoreCredentialGateway: RestoreCredentialGateway) {
    suspend operator fun invoke() {
        restoreCredentialGateway.clear()
        sessionStore.clear()
    }
}
