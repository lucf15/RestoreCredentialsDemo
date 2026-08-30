package io.github.lucf15.restorecredentials.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination : NavKey {
    @Serializable data object Splash : Destination

    @Serializable data object SignIn : Destination

    @Serializable data object Home : Destination
}
