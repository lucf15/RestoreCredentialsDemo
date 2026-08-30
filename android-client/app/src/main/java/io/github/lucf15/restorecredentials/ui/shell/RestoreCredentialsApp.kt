package io.github.lucf15.restorecredentials.ui.shell

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.lucf15.restorecredentials.ui.navigation.Destination
import io.github.lucf15.restorecredentials.ui.navigation.Navigator
import io.github.lucf15.restorecredentials.ui.screens.home.HomeScreen
import io.github.lucf15.restorecredentials.ui.screens.signin.SignInScreen
import io.github.lucf15.restorecredentials.ui.screens.splash.SplashScreen

@Composable
fun RestoreCredentialsApp() {
    val backStack = rememberNavBackStack(Destination.Splash)
    val navigator = remember(backStack) { Navigator(backStack) }

    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()),
        entryProvider =
            entryProvider {
                entry<Destination.Splash> { SplashScreen(onSignedIn = navigator::toHome, onNeedsSignIn = navigator::toSignIn) }
                entry<Destination.SignIn> { SignInScreen(onSignedIn = navigator::toHome) }
                entry<Destination.Home> { HomeScreen(onSignedOut = navigator::toSignIn) }
            },
    )
}
