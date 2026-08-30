package io.github.lucf15.restorecredentials.ui.screens.splash

import io.github.lucf15.restorecredentials.ui.base.UiEffect

interface SplashContract {
    sealed class Effect : UiEffect {
        data object SignedIn : Effect()

        data object NeedsSignIn : Effect()
    }
}
