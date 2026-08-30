package io.github.lucf15.restorecredentials.ui.base

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface TextResource {
    data class Plain(val value: String) : TextResource

    data class Resource(@StringRes val id: Int, val args: List<Any> = emptyList()) : TextResource
}

fun textResource(@StringRes id: Int, vararg args: Any): TextResource = TextResource.Resource(id, args.toList())

@Composable
fun TextResource.resolve(): String =
    when (this) {
        is TextResource.Plain -> value
        is TextResource.Resource -> stringResource(id, *args.map { arg -> if (arg is TextResource) arg.resolve() else arg }.toTypedArray())
    }
