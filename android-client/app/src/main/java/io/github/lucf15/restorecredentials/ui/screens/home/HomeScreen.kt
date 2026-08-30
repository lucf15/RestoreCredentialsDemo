package io.github.lucf15.restorecredentials.ui.screens.home

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.github.lucf15.restorecredentials.R
import io.github.lucf15.restorecredentials.ui.base.produceStateWithLifecycle
import io.github.lucf15.restorecredentials.ui.designsystem.components.AppButton
import io.github.lucf15.restorecredentials.ui.designsystem.components.AppHeader
import io.github.lucf15.restorecredentials.ui.designsystem.components.AppScaffold
import io.github.lucf15.restorecredentials.ui.designsystem.theme.AppTheme
import io.github.lucf15.restorecredentials.ui.designsystem.theme.AppThemeProvider
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(onSignedOut: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<HomeViewModel>()
    viewModel.produceStateWithLifecycle { produceState() }

    LaunchedEffect(viewModel) {
        launch { viewModel.effect.collect { effect -> if (effect is HomeContract.Effect.SignedOut) onSignedOut() } }
    }

    HomeScreenContent(state = { viewModel.state }, onEvent = viewModel::onEvent, modifier = modifier)
}

@Composable
internal fun HomeScreenContent(state: () -> HomeContract.State, onEvent: (HomeContract.Event) -> Unit, modifier: Modifier = Modifier) {
    AppScaffold(
        modifier = modifier.fillMaxSize(),
        header = { padding -> AppHeader(title = stringResource(R.string.home_title), insetPadding = padding) },
        footer = { padding ->
            Box(modifier = Modifier.fillMaxWidth().padding(padding).padding(bottom = 20.dp)) {
                AppButton(
                    text = stringResource(R.string.home_sign_out),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEvent(HomeContract.Event.SignOut) },
                )
            }
        },
    ) { padding ->
        val current = state()
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(top = AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText(stringResource(R.string.home_signed_in_as), style = AppTheme.typography.sectionTitle.copy(color = AppTheme.colors.textSecondary))
                BasicText(current.username, style = AppTheme.typography.cardTitle.copy(color = AppTheme.colors.textPrimary))
            }
            BasicText(
                stringResource(R.string.home_restore_active_caption),
                style = AppTheme.typography.body.copy(color = AppTheme.colors.textSecondary),
            )
        }
    }
}

private class HomeStatePreviewProvider : PreviewParameterProvider<HomeContract.State> {
    override val values = sequenceOf(HomeContract.State.Immutable(username = "luca_demo"))
}

@Preview(name = "Home — Light", showBackground = true, device = Devices.PHONE)
@Preview(name = "Home — Dark", showBackground = true, device = Devices.PHONE, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenStatesPreview(@PreviewParameter(HomeStatePreviewProvider::class) state: HomeContract.State) {
    AppThemeProvider(dynamicColor = false) { HomeScreenContent(state = { state }, onEvent = {}) }
}
