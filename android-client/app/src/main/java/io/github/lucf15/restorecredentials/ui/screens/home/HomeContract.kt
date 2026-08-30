package io.github.lucf15.restorecredentials.ui.screens.home

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.lucf15.restorecredentials.ui.base.UiEffect
import io.github.lucf15.restorecredentials.ui.base.UiEvent

interface HomeContract {

    @Stable
    interface State {
        val username: String

        data class Immutable(override val username: String = "") : State

        @Stable
        class SnapshotMutable(initial: Immutable) : State {
            override var username: String by mutableStateOf(initial.username)
        }
    }

    sealed class Event : UiEvent {
        data object SignOut : Event()
    }

    sealed class Effect : UiEffect {
        data object SignedOut : Effect()
    }
}

fun HomeContract.State.Immutable.toSnapshotMutable(): HomeContract.State.SnapshotMutable = HomeContract.State.SnapshotMutable(this)
