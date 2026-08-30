package io.github.lucf15.restorecredentials.ui.screens.signin

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.github.lucf15.restorecredentials.R
import io.github.lucf15.restorecredentials.ui.base.TextResource
import io.github.lucf15.restorecredentials.ui.base.resolve
import io.github.lucf15.restorecredentials.ui.designsystem.components.AppButton
import io.github.lucf15.restorecredentials.ui.designsystem.components.AppHeader
import io.github.lucf15.restorecredentials.ui.designsystem.components.AppLoader
import io.github.lucf15.restorecredentials.ui.designsystem.components.AppScaffold
import io.github.lucf15.restorecredentials.ui.designsystem.components.AppTextField
import io.github.lucf15.restorecredentials.ui.designsystem.theme.AppTheme
import io.github.lucf15.restorecredentials.ui.designsystem.theme.AppThemeProvider
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SignInScreen(onSignedIn: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<SignInViewModel>()

    LaunchedEffect(viewModel) {
        launch { viewModel.effect.collect { effect -> if (effect is SignInContract.Effect.SignedIn) onSignedIn() } }
    }

    SignInScreenContent(state = { viewModel.state }, onEvent = viewModel::onEvent, modifier = modifier)
}

@Composable
internal fun SignInScreenContent(state: () -> SignInContract.State, onEvent: (SignInContract.Event) -> Unit, modifier: Modifier = Modifier) {
    AppScaffold(
        modifier = modifier.fillMaxSize(),
        header = { padding -> AppHeader(title = stringResource(R.string.signin_title), insetPadding = padding) },
        footer = { padding ->
            val current = state()
            Box(modifier = Modifier.fillMaxWidth().padding(padding).padding(bottom = 20.dp)) {
                if (current.isSubmitting) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { AppLoader(modifier = Modifier.size(28.dp)) }
                } else {
                    AppButton(
                        text = stringResource(R.string.signin_submit),
                        filled = true,
                        enabled = current.username.isNotBlank() && current.password.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onEvent(SignInContract.Event.Submit) },
                    )
                }
            }
        },
    ) { padding ->
        val current = state()
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(top = AppTheme.spacing.lg).verticalScroll(rememberScrollState()).imePadding(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    value = current.username,
                    onValueChange = { onEvent(SignInContract.Event.UsernameChanged(it)) },
                    label = stringResource(R.string.signin_username_label),
                )
                AppTextField(
                    value = current.password,
                    onValueChange = { onEvent(SignInContract.Event.PasswordChanged(it)) },
                    label = stringResource(R.string.signin_password_label),
                    isPassword = true,
                )
                current.error?.let { error ->
                    BasicText(error.resolve(), style = AppTheme.typography.caption.copy(color = AppTheme.colors.textError))
                }
            }

            BasicText(stringResource(R.string.signin_subtitle), style = AppTheme.typography.body.copy(color = AppTheme.colors.textSecondary))
        }
    }
}

private class SignInStatePreviewProvider : PreviewParameterProvider<SignInContract.State> {
    override val values =
        sequenceOf(
            SignInContract.State.Immutable(),
            SignInContract.State.Immutable(username = "luca_demo", password = "hunter2"),
            SignInContract.State.Immutable(username = "luca_demo", password = "hunter2", isSubmitting = true),
            SignInContract.State.Immutable(
                username = "luca_demo",
                password = "wrong",
                error = TextResource.Plain("Incorrect username or password"),
            ),
        )
}

@Preview(name = "Sign in states — Light", showBackground = true, device = Devices.PHONE)
@Preview(name = "Sign in states — Dark", showBackground = true, device = Devices.PHONE, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SignInScreenStatesPreview(@PreviewParameter(SignInStatePreviewProvider::class) state: SignInContract.State) {
    AppThemeProvider(dynamicColor = false) { SignInScreenContent(state = { state }, onEvent = {}) }
}
