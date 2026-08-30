package io.github.lucf15.restorecredentials.ui.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun AppThemeProvider(isDark: Boolean = isSystemInDarkTheme(), dynamicColor: Boolean = true, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colors =
        remember(isDark, dynamicColor, context) {
            when {
                dynamicColor && dynamicColorAvailable() -> dynamicAppColors(context, isDark)
                isDark -> DarkColors
                else -> LightColors
            }
        }

    CompositionLocalProvider(LocalAppColors provides colors, LocalIsDarkTheme provides isDark) {
        Box(modifier = Modifier.fillMaxSize().background(colors.background)) { content() }
    }
}

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current

    val typography: AppTypography
        @Composable get() = AppTypographyDefault

    val spacing: AppSpacing
        get() = Spacing
}

val LocalAppColors = staticCompositionLocalOf<AppColors> { error("No AppColors provided") }
val LocalIsDarkTheme = staticCompositionLocalOf { false }
