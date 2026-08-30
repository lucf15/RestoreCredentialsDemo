package io.github.lucf15.restorecredentials.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun <T> T.produceStateWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    producer: suspend T.() -> Unit,
) {
    LaunchedEffect(this, lifecycleOwner, minActiveState) { lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) { producer() } }
}
