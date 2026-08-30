package io.github.lucf15.restorecredentials.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lucf15.restorecredentials.domain.repository.SessionStore
import io.github.lucf15.restorecredentials.domain.usecase.SignOutUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class HomeViewModel(private val sessionStore: SessionStore, private val signOut: SignOutUseCase) : ViewModel() {

    val state: HomeContract.State field = HomeContract.State.Immutable().toSnapshotMutable()

    val effect: SharedFlow<HomeContract.Effect> field = MutableSharedFlow()

    suspend fun produceState() {
        sessionStore.session.filterNotNull().collect { session -> state.username = session.username }
    }

    fun onEvent(event: HomeContract.Event) {
        when (event) {
            HomeContract.Event.SignOut -> onSignOut()
        }
    }

    private fun onSignOut() {
        viewModelScope.launch {
            signOut()
            effect.emit(HomeContract.Effect.SignedOut)
        }
    }
}
