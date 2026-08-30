package io.github.lucf15.restorecredentials.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

class Navigator(private val backStack: NavBackStack<NavKey>) {
    fun toSignIn() = replaceStackWith(Destination.SignIn)

    fun toHome() = replaceStackWith(Destination.Home)

    private fun replaceStackWith(destination: Destination) {
        backStack.clear()
        backStack.add(destination)
    }
}
