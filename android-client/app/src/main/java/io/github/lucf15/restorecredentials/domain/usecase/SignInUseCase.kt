package io.github.lucf15.restorecredentials.domain.usecase

import io.github.lucf15.restorecredentials.domain.model.AuthSession
import io.github.lucf15.restorecredentials.domain.repository.AuthRepository
import io.github.lucf15.restorecredentials.domain.repository.SessionStore

class SignInUseCase(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore,
    private val registerRestoreCredential: RegisterRestoreCredentialUseCase,
) {
    suspend operator fun invoke(username: String, password: String): AuthSession {
        val session = authRepository.login(username, password)
        sessionStore.save(session)
        runCatching { registerRestoreCredential(session.accessToken) }
        return session
    }
}
