package io.github.lucf15.restorecredentials.ui.screens.splash

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.lucf15.restorecredentials.R
import io.github.lucf15.restorecredentials.ui.designsystem.components.AppLoader
import io.github.lucf15.restorecredentials.ui.designsystem.theme.AppTheme
import io.github.lucf15.restorecredentials.ui.designsystem.theme.AppThemeProvider
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(onSignedIn: () -> Unit, onNeedsSignIn: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<SplashViewModel>()

    LaunchedEffect(viewModel) {
        launch { viewModel.resolveStartDestination() }
        launch {
            viewModel.effect.collect { effect ->
                when (effect) {
                    SplashContract.Effect.SignedIn -> onSignedIn()
                    SplashContract.Effect.NeedsSignIn -> onNeedsSignIn()
                }
            }
        }
    }

    SplashScreenContent(modifier = modifier)
}

@Composable
internal fun SplashScreenContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md, Alignment.CenterVertically),
    ) {
        BasicText(stringResource(R.string.app_name), style = AppTheme.typography.headerTitle.copy(color = AppTheme.colors.textPrimary))
        AppLoader(modifier = Modifier.size(40.dp))
    }
}

@Preview(name = "Splash — Light", showBackground = true, device = Devices.PHONE)
@Preview(name = "Splash — Dark", showBackground = true, device = Devices.PHONE, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SplashScreenPreview() {
    AppThemeProvider(dynamicColor = false) { SplashScreenContent() }
}
