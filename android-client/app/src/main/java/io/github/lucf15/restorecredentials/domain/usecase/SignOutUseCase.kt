package io.github.lucf15.restorecredentials.domain.usecase

import io.github.lucf15.restorecredentials.domain.repository.AuthRepository
import io.github.lucf15.restorecredentials.domain.repository.RestoreCredentialGateway
import io.github.lucf15.restorecredentials.domain.repository.SessionStore
import kotlinx.coroutines.flow.first

class SignOutUseCase(
    private val sessionStore: SessionStore,
    private val restoreCredentialGateway: RestoreCredentialGateway,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() {
        sessionStore.session.first()?.refreshToken?.let { token ->
            runCatching { authRepository.logout(token) }
        }
        restoreCredentialGateway.clear()
        sessionStore.clear()
    }
}
