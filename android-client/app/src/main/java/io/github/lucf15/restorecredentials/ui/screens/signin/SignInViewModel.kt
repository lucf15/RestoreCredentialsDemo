package io.github.lucf15.restorecredentials.ui.screens.signin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lucf15.restorecredentials.R
import io.github.lucf15.restorecredentials.domain.usecase.SignInUseCase
import io.github.lucf15.restorecredentials.ui.base.textResource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class SignInViewModel(private val signIn: SignInUseCase) : ViewModel() {

    val state: SignInContract.State field = SignInContract.State.Immutable().toSnapshotMutable()

    val effect: SharedFlow<SignInContract.Effect> field = MutableSharedFlow()

    fun onEvent(event: SignInContract.Event) {
        when (event) {
            is SignInContract.Event.UsernameChanged -> state.username = event.value
            is SignInContract.Event.PasswordChanged -> state.password = event.value
            SignInContract.Event.Submit -> onSubmit()
        }
    }

    private fun onSubmit() {
        val username = state.username.trim()
        val password = state.password
        if (username.isEmpty() || password.isEmpty()) return

        state.isSubmitting = true
        state.error = null
        viewModelScope.launch {
            runCatching { signIn(username, password) }
                .onSuccess {
                    state.isSubmitting = false
                    effect.emit(SignInContract.Effect.SignedIn)
                }
                .onFailure { error -> onSignInFailed(error) }
        }
    }

    private fun onSignInFailed(error: Throwable) {
        Log.e("SignInViewModel", "Sign in failed", error)
        state.isSubmitting = false
        state.error = textResource(R.string.signin_generic_error)
    }
}
