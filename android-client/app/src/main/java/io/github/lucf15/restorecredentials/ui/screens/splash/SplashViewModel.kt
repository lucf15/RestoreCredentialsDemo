package io.github.lucf15.restorecredentials.ui.screens.splash

import androidx.lifecycle.ViewModel
import io.github.lucf15.restorecredentials.domain.repository.SessionStore
import io.github.lucf15.restorecredentials.domain.usecase.TryRestoreSignInUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first

class SplashViewModel(private val sessionStore: SessionStore, private val tryRestoreSignIn: TryRestoreSignInUseCase) : ViewModel() {

    val effect: SharedFlow<SplashContract.Effect> field = MutableSharedFlow()

    suspend fun resolveStartDestination() {
        val alreadySignedIn = sessionStore.session.first() != null
        val signedIn = alreadySignedIn || runCatching { tryRestoreSignIn() }.getOrDefault(false)
        effect.emit(if (signedIn) SplashContract.Effect.SignedIn else SplashContract.Effect.NeedsSignIn)
    }
}
