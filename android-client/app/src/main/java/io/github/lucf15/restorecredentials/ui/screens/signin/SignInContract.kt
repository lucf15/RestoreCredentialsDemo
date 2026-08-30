package io.github.lucf15.restorecredentials.ui.screens.signin

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.lucf15.restorecredentials.ui.base.TextResource
import io.github.lucf15.restorecredentials.ui.base.UiEffect
import io.github.lucf15.restorecredentials.ui.base.UiEvent

interface SignInContract {

    @Stable
    interface State {
        val username: String
        val password: String
        val isSubmitting: Boolean
        val error: TextResource?

        data class Immutable(
            override val username: String = "",
            override val password: String = "",
            override val isSubmitting: Boolean = false,
            override val error: TextResource? = null,
        ) : State

        @Stable
        class SnapshotMutable(initial: Immutable) : State {
            override var username: String by mutableStateOf(initial.username)
            override var password: String by mutableStateOf(initial.password)
            override var isSubmitting: Boolean by mutableStateOf(initial.isSubmitting)
            override var error: TextResource? by mutableStateOf(initial.error)
        }
    }

    sealed class Event : UiEvent {
        data class UsernameChanged(val value: String) : Event()

        data class PasswordChanged(val value: String) : Event()

        data object Submit : Event()
    }

    sealed class Effect : UiEffect {
        data object SignedIn : Effect()
    }
}

fun SignInContract.State.Immutable.toSnapshotMutable(): SignInContract.State.SnapshotMutable = SignInContract.State.SnapshotMutable(this)
