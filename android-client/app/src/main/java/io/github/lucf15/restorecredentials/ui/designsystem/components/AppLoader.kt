package io.github.lucf15.restorecredentials.ui.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.lucf15.restorecredentials.ui.designsystem.theme.AppTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

sealed interface AppLoaderState {
    data object Loading : AppLoaderState

    data class Progress(val fraction: Float) : AppLoaderState
}

@Composable
fun AppLoader(modifier: Modifier = Modifier, color: Color = AppTheme.colors.primary, state: () -> AppLoaderState = { AppLoaderState.Loading }) {
    val infiniteTransition = rememberInfiniteTransition(label = "loaderAnimation")

    val phase =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
            label = "phase",
        )

    Canvas(modifier = modifier) {
        val currentState = state()
        val isRefreshing = currentState is AppLoaderState.Loading
        val progress =
            when (currentState) {
                is AppLoaderState.Loading -> 1f
                is AppLoaderState.Progress -> currentState.fraction
            }
        val currentPhase = phase.value

        val baseDotRadius = size.width * 0.12f
        val maxLoaderRadius = (size.width / 2) - baseDotRadius
        val currentLoaderRadius = if (isRefreshing) maxLoaderRadius else maxLoaderRadius * progress.coerceIn(0f, 1f)
        val currentAlpha = if (isRefreshing) 1f else progress.coerceIn(0f, 1f)
        val angleStep = 360f / DOT_COUNT
        for (i in 0 until DOT_COUNT) {
            val dotAngle = i * angleStep
            val scale =
                if (isRefreshing) {
                    val diff = (currentPhase - dotAngle + 360f) % 360f
                    0.5f + 0.5f * (1f - (diff / 360f)).pow(4)
                } else {
                    0.5f
                }
            val angleRad = (dotAngle * PI / 180f).toFloat()
            val x = center.x + currentLoaderRadius * cos(angleRad)
            val y = center.y + currentLoaderRadius * sin(angleRad)
            drawCircle(color = color.copy(alpha = currentAlpha), radius = baseDotRadius * scale, center = Offset(x, y))
        }
    }
}

private const val DOT_COUNT = 8
